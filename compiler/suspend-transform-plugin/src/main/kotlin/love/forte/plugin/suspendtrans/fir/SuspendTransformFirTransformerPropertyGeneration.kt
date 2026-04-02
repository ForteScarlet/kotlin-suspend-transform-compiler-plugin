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
import org.jetbrains.kotlin.fir.FirResolvePhase
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.FirPropertyBodyResolveState
import org.jetbrains.kotlin.fir.declarations.UnresolvedDeprecationProvider
import org.jetbrains.kotlin.fir.declarations.builder.buildProperty
import org.jetbrains.kotlin.fir.declarations.builder.buildPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.declarations.resolvePhase
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.resolve.FirFunctionTarget
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.fir.types.withReplacedConeType
import org.jetbrains.kotlin.name.CallableId

/**
 * Generates synthetic properties for suspend declarations whose marker annotation
 * requests property-style exposure.
 */
@OptIn(SymbolInternals::class)
internal fun SuspendTransformFirTransformer.generateSyntheticProperties(
    callableId: CallableId,
    context: MemberGenerationContext?
): List<FirPropertySymbol> {
    val owner = context?.owner ?: return emptyList()
    val funcMap = cache.getValue(FirCacheKey(owner, context.declaredScope))
        ?.get(callableId.callableName)
        ?: return emptyList()

    return buildList {
        for ((originalFunSymbol, funData) in funcMap) {
            generateSyntheticProperty(callableId, owner, originalFunSymbol, funData)?.also(::add)
        }
    }
}

/**
 * Builds one synthetic property plus getter body for a suspend source function when
 * the annotation metadata targets property generation.
 */
@OptIn(SymbolInternals::class)
private fun SuspendTransformFirTransformer.generateSyntheticProperty(
    callableId: CallableId,
    owner: FirClassSymbol<*>,
    originalFunSymbol: FirNamedFunctionSymbol,
    funData: SyntheticFunData,
): FirPropertySymbol? {
    val annotationData = funData.annotationData

    if (!annotationData.asProperty) {
        return null
    }

    val isOverride =
        checkSyntheticFunctionIsOverrideBasedOnSourceFunction(funData, originalFunSymbol, checkContext)

    // generate
    val original = originalFunSymbol.fir

    val (functionAnnotations, propertyAnnotations, includeToOriginal) =
        copyAnnotations(original, funData)

    val pSymbol = FirRegularPropertySymbol(callableId)

//                val pKey = SuspendTransformPluginKey(
//                    data = SuspendTransformUserDataFir(
//                        markerId = uniqueFunHash,
//                        originSymbol = original.symbol.asOriginSymbol(
//                            targetMarkerAnnotation,
//                            typeParameters = original.typeParameters,
//                            valueParameters = original.valueParameters,
//                            original.returnTypeRef.coneTypeOrNull?.classId,
//                            session
//                        ),
//                        asProperty = true,
//                        transformer = funData.transformer
//                    )
//                )
    val pKey = SuspendTransformK2V3Key

    val originalReturnType = original.returnTypeRef

    val originalTypeParameterCache: MutableList<CopiedTypeParameterPair> = mutableListOf()
    val copiedReturnType = originalReturnType.withReplacedConeType(
        originalReturnType.coneTypeOrNull?.copyConeTypeOrSelf(originalTypeParameterCache, firSession)
    )

    // copy完了再resolve，这样里面包的type parameter就不会有问题了（如果有type parameter的话）
    val resolvedReturnType = resolveReturnType(funData.transformer, copiedReturnType)

    val newFunTarget = FirFunctionTarget(null, isLambda = false)

    val property = buildProperty {
        symbol = pSymbol
        name = callableId.callableName
        isLocal = false
        source = original.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
        resolvePhase = original.resolvePhase
        moduleData = original.moduleData
        origin = pKey.origin
        attributes = original.attributes.copy()
        status = original.status.copy(
            isSuspend = false,
            isFun = false,
            isInner = false,
            modality = original.syntheticModifier,
            isOverride = isOverride || isOverridable(
                firSession,
                callableId.callableName,
                original,
                owner,
                isProperty = true
            ),
        )

        isVar = false
        // Copy return type
        returnTypeRef = resolvedReturnType
        deprecationsProvider = UnresolvedDeprecationProvider //original.deprecationsProvider
        containerSource = original.containerSource
        dispatchReceiverType = original.dispatchReceiverType
        contextParameters.addAll(original.contextParameters)
//                contextReceivers.addAll(original.contextReceivers)
        // annotations
        annotations.addAll(propertyAnnotations)
        typeParameters.addAll(original.typeParameters)
        resolvePhase = FirResolvePhase.BODY_RESOLVE
        backingField = null
        bodyResolveState = FirPropertyBodyResolveState.NOTHING_RESOLVED

        getter = buildPropertyAccessor {
            propertySymbol = pSymbol
            val propertyAccessorSymbol = FirPropertyAccessorSymbol()
            symbol = propertyAccessorSymbol
            isGetter = true
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = original.moduleData

            // annotations
            annotations.addAll(functionAnnotations)

            returnTypeRef = resolvedReturnType
            origin = pKey.origin

            status = original.status.copy(
                isSuspend = false,
                isFun = false,
                isInner = false,
                modality = original.syntheticModifier,
                isOverride = false, // funData.isOverride,
                //                            visibility = this@buildProperty.status
            )

            valueParameters.addAll(original.valueParameters)

            copyParameters(
                newFunSymbol = propertyAccessorSymbol,
                session = firSession,
                originalTypeParameterCache = originalTypeParameterCache,
                copyReturnType = false,
            )

            val thisValueParameters = this.valueParameters

            body = generateSyntheticFunctionBody(
                original,
                originalFunSymbol,
                owner,
                emptyList(),
                null,
                propertyAccessorSymbol,
                thisValueParameters,
                funData.transformerFunctionSymbol,
                newFunTarget,
                funData.transformer
            )
        }.also { getter ->
            newFunTarget.bind(getter)
        }
    }

    // 在原函数上附加的annotations
    original.includeAnnotations(includeToOriginal)

    return property.symbol
}
