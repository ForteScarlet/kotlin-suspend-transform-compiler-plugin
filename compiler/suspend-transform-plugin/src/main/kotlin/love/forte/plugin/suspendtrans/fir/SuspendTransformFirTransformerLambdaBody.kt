/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package love.forte.plugin.suspendtrans.fir

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.FirResolvePhase
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.builder.buildAnonymousFunction
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildReturnExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.references.builder.buildExplicitThisReference
import org.jetbrains.kotlin.fir.references.builder.buildImplicitThisReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.FirFunctionTarget
import org.jetbrains.kotlin.fir.resolve.toQualifiedAccess
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 * Builds the suspend lambda passed into the configured transformer bridge.
 */
@OptIn(SymbolInternals::class)
internal fun SuspendTransformFirTransformer.buildSyntheticLambda(
    originFunc: FirNamedFunction,
    originFunSymbol: FirNamedFunctionSymbol,
    owner: FirClassSymbol<*>,
    thisContextParameters: List<FirValueParameter>,
    thisReceiverParameter: FirReceiverParameter?,
    thisValueParameters: List<FirValueParameter>,
    lambdaTarget: FirFunctionTarget,
): FirAnonymousFunction {
    return buildAnonymousFunction {
        source = originFunSymbol.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
        resolvePhase = FirResolvePhase.BODY_RESOLVE
        // this.resolvePhase = FirResolvePhase.RAW_FIR
        isLambda = true
        moduleData = originFunSymbol.moduleData
        // this.origin = FirDeclarationOrigin.Source
        // this.origin = FirDeclarationOrigin.Synthetic.FakeFunction
        origin = FirDeclarationOrigin.Plugin(SuspendTransformK2V3Key)
        returnTypeRef = originFunSymbol.resolvedReturnTypeRef
        hasExplicitParameterList = false
        // this.status = FirResolvedDeclarationStatusImpl.DEFAULT_STATUS_FOR_SUSPEND_FUNCTION_EXPRESSION
        status = status.copy(isSuspend = true)
        symbol = FirAnonymousFunctionSymbol()
        body = buildSingleExpressionBlock(
            buildReturnExpression {
                target = lambdaTarget
                result = buildOriginFunctionCall(
                    originFunc,
                    originFunSymbol,
                    owner,
                    thisContextParameters,
                    thisReceiverParameter,
                    thisValueParameters,
                )
            }
        )

        typeRef = buildResolvedTypeRef {
            coneType = ClassId.topLevel(FqName("kotlin.SuspendFunction0"))
                .createConeType(firSession, arrayOf(originFunSymbol.resolvedReturnType))
        }
    }
}

/**
 * Recreates the call to the original suspend function inside the generated lambda.
 */
@OptIn(SymbolInternals::class)
private fun SuspendTransformFirTransformer.buildOriginFunctionCall(
    originFunc: FirNamedFunction,
    originFunSymbol: FirNamedFunctionSymbol,
    owner: FirClassSymbol<*>,
    thisContextParameters: List<FirValueParameter>,
    thisReceiverParameter: FirReceiverParameter?,
    thisValueParameters: List<FirValueParameter>,
) = buildFunctionCall {
    // Call original fun
    coneTypeOrNull = originFunSymbol.resolvedReturnTypeRef.coneType
    source = null
    calleeReference = buildResolvedNamedReference {
        source = null
        name = originFunSymbol.name
        resolvedSymbol = originFunSymbol
    }

    val originValueParameters = originFunc.valueParameters

    dispatchReceiver = buildOwnerThisReceiverExpression(originFunSymbol, owner)

    contextArguments.addAll(thisContextParameters.map { receiver ->
        buildThisReceiverExpression {
            coneTypeOrNull = receiver.returnTypeRef.coneTypeOrNull
            source = null
            calleeReference = buildExplicitThisReference {
                source = null
                // labelName = receiver.labelName?.asString()
            }
        }
    })

    // TODO What is explicitReceiver?
    // this.explicitReceiver

    extensionReceiver = thisReceiverParameter?.let { receiverParameter ->
        buildThisReceiverExpression {
            coneTypeOrNull = receiverParameter.typeRef.coneTypeOrNull
            source = null
            calleeReference = buildImplicitThisReference {
                boundSymbol = receiverParameter.symbol
            }
        }
    }

    if (thisValueParameters.isNotEmpty()) {
        argumentList = buildResolvedArgumentList(
            null,
            mapping = linkedMapOf<FirExpression, FirValueParameter>().apply {
                thisValueParameters.forEachIndexed { index, thisParam ->
                    val qualifiedAccess = thisParam.toQualifiedAccess()
                    put(qualifiedAccess, originValueParameters[index])
                }
            }
        )
    }
}
