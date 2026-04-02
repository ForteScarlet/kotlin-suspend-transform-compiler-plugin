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

import love.forte.plugin.suspendtrans.utils.includeAnnotations
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.builder.buildNamedFunctionCopy
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.resolve.FirFunctionTarget
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId

/**
 * Generates synthetic functions for one source suspend function and appends them to [results].
 */
@OptIn(SymbolInternals::class)
internal fun SuspendTransformFirTransformer.generateSyntheticFunctions(
    callableId: CallableId,
    owner: FirClassSymbol<*>,
    originFunSymbol: FirNamedFunctionSymbol,
    funData: SyntheticFunData,
    results: MutableList<FirNamedFunctionSymbol>,
) {
    generateSyntheticFunction(callableId, owner, originFunSymbol, funData)?.also(results::add)
}

/**
 * Builds one generated function declaration for a suspend source function when the
 * annotation metadata targets function generation.
 */
@OptIn(SymbolInternals::class)
private fun SuspendTransformFirTransformer.generateSyntheticFunction(
    callableId: CallableId,
    owner: FirClassSymbol<*>,
    originFunSymbol: FirNamedFunctionSymbol,
    funData: SyntheticFunData,
): FirNamedFunctionSymbol? {
    val annotationData = funData.annotationData
    if (annotationData.asProperty) {
        return null
    }

    // Check the overridden for isOverride based on source function (func) 's overridden
    val isOverride =
        checkSyntheticFunctionIsOverrideBasedOnSourceFunction(funData, originFunSymbol, checkContext)

    // generate
    val originFunc = originFunSymbol.fir

    val (functionAnnotations, _, includeToOriginal) = copyAnnotations(originFunc, funData)

    val newFunSymbol = FirNamedFunctionSymbol(callableId)

//            val key = SuspendTransformPluginKey(
//                data = SuspendTransformUserDataFir(
//                    markerId = UUID.randomUUID().toString(),
//                    originSymbol = originFunc.symbol.asOriginSymbol(
//                        targetMarkerAnnotation,
//                        typeParameters = originFunc.typeParameters,
//                        valueParameters = originFunc.valueParameters,
//                        originFunc.returnTypeRef.coneTypeOrNull?.classId,
//                        session,
//                    ),
//                    asProperty = false,
//                    transformer = funData.transformer
//                )
//            )
    val key = SuspendTransformK2V3Key

    val newFunTarget = FirFunctionTarget(null, isLambda = false)
    val newFun = buildNamedFunctionCopy(originFunc) {
        origin = key.origin
        source = originFunc.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
        name = callableId.callableName
        symbol = newFunSymbol
        status = originFunc.status.copy(
            isSuspend = false,
            modality = originFunc.syntheticModifier,
            // Use OPEN and `override` is unnecessary. .. ... Maybe?
            isOverride = isOverride || isOverridable(
                firSession,
                callableId.callableName,
                originFunc,
                owner,
                isProperty = false,
            ),
        )

        // Copy the typeParameters.
        // Otherwise, in functions like the following, an error will occur
        // suspend fun <A> data(value: A): T = ...
        // Functions for which function-scoped generalizations (`<A>`) exist.
        // In the generated IR, data and dataBlocking will share an `A`, generating the error.
        // The error: Duplicate IR node
        //     [IR VALIDATION] JvmIrValidationBeforeLoweringPhase: Duplicate IR node: TYPE_PARAMETER name:A index:0 variance: superTypes:[kotlin.Any?] reified:false of FUN GENERATED[...]
        copyParameters(firSession)

        // resolve returnType (with wrapped) after copyParameters
        returnTypeRef = resolveReturnType(funData.transformer, returnTypeRef)

        val thisReceiverParameter = this.receiverParameter
        val thisContextParameters = this.contextParameters
        val thisValueParameters = this.valueParameters

        annotations.clear()
        annotations.addAll(functionAnnotations)

        body = generateSyntheticFunctionBody(
            originFunc,
            originFunSymbol,
            owner,
            thisContextParameters,
            thisReceiverParameter,
            newFunSymbol,
            thisValueParameters,
            funData.transformerFunctionSymbol,
            newFunTarget,
            funData.transformer
        )
    }

    newFunTarget.bind(newFun)

    // 在原函数上附加的annotations
    originFunc.includeAnnotations(includeToOriginal)

    return newFun.symbol
}
