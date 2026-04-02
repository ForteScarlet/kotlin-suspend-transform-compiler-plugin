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

import love.forte.plugin.suspendtrans.configuration.Transformer
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildReturnExpression
import org.jetbrains.kotlin.fir.resolve.FirFunctionTarget
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.coneType

/**
 * Builds the FIR body of a generated synthetic bridge function or accessor.
 */
@OptIn(SymbolInternals::class)
@Suppress("UNUSED_PARAMETER")
internal fun SuspendTransformFirTransformer.generateSyntheticFunctionBody(
    originFunc: FirNamedFunction,
    originFunSymbol: FirNamedFunctionSymbol,
    owner: FirClassSymbol<*>,
    thisContextParameters: List<FirValueParameter>,
    thisReceiverParameter: FirReceiverParameter?,
    newFunSymbol: FirBasedSymbol<*>,
    thisValueParameters: List<FirValueParameter>,
    bridgeFunSymbol: FirNamedFunctionSymbol,
    newFunTarget: FirFunctionTarget,
    transformer: Transformer
): FirBlock = buildBlock {
    // Plugin-generated declarations still need a synthetic source element.
    source = originFunSymbol.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)

    val lambdaTarget = FirFunctionTarget(null, isLambda = true)
    val lambda = buildSyntheticLambda(
        originFunc,
        originFunSymbol,
        owner,
        thisContextParameters,
        thisReceiverParameter,
        thisValueParameters,
        lambdaTarget,
    )
    lambdaTarget.bind(lambda)

    val returnType = resolveReturnType(transformer, originFunc.returnTypeRef)

    statements.add(
        buildReturnExpression {
            target = newFunTarget
            result = buildBridgeFunctionCall(
                originFunSymbol,
                owner,
                bridgeFunSymbol,
                lambda,
                returnType.coneType,
            )
        }
    )
}
