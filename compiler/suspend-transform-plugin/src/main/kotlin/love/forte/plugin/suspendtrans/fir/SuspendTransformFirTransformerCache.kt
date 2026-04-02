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

import love.forte.plugin.suspendtrans.configuration.MarkAnnotation
import love.forte.plugin.suspendtrans.configuration.TargetPlatform
import love.forte.plugin.suspendtrans.configuration.Transformer
import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.resolve.providers.dependenciesSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
import org.jetbrains.kotlin.fir.scopes.processAllFunctions
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves and caches the coroutine scope class required by generated bridge calls.
 */
internal fun SuspendTransformFirTransformer.initScopeSymbol() {
    val classId = ClassId(
        FqName.fromSegments(listOf("kotlinx", "coroutines")),
        Name.identifier("CoroutineScope")
    )

    if (!isCoroutineScopeSymbolInitialized()) {
        coroutineScopeSymbol = firSession.symbolProvider.getClassLikeSymbolByClassId(classId)
            ?: firSession.dependenciesSymbolProvider.getClassLikeSymbolByClassId(classId)
            ?: error("Cannot resolve `kotlinx.coroutines.CoroutineScope` symbol.")
    }
}

/**
 * Resolves configured bridge functions for the current module and caches the
 * successful lookups for reuse across classes.
 */
@Suppress("UNUSED_PARAMETER")
internal fun SuspendTransformFirTransformer.initTransformerFunctionSymbolMap(
    classSymbol: FirClassSymbol<*>,
    memberScope: FirClassDeclaredMemberScope?
): Map<Transformer, FirNamedFunctionSymbol> {
    // 尝试找到所有配置的 bridge function, 例如 `runBlocking` 等
    val symbolProvider = firSession.symbolProvider
    val dependenciesSymbolProvider = firSession.dependenciesSymbolProvider

    val map = mutableMapOf<Transformer, FirNamedFunctionSymbol>()

    suspendTransformConfiguration.transformers
        .forEach { (_, transformerList) ->
            for (transformer in transformerList) {
                val transformFunctionInfo = transformer.transformFunctionInfo
                val packageName = transformFunctionInfo.packageName
                val functionName = transformFunctionInfo.functionName

                // TODO 校验funcs?

                val functionNameIdentifier = Name.identifier(functionName)

                val transformerFunctionSymbols =
                    symbolProvider.getTopLevelFunctionSymbols(
                        packageName.fqn,
                        functionNameIdentifier
                    ).ifEmpty {
                        dependenciesSymbolProvider.getTopLevelFunctionSymbols(
                            packageName.fqn,
                            functionNameIdentifier
                        )
                    }

                if (transformerFunctionSymbols.isNotEmpty()) {
                    if (transformerFunctionSymbols.size == 1) {
                        transformerFunctionSymbolMap[transformer] = transformerFunctionSymbols.first()
                        map[transformer] = transformerFunctionSymbols.first()
                    } else {
                        error("Found multiple transformer function symbols for transformer: $transformer")
                    }
                } else {
                    // 有时候在不同平台中寻找，可能会找不到，例如在jvm中找不到js的函数
                    // error("Cannot find transformer function symbol $packageName.$functionName (${firSession.moduleData.platform}) for transformer: $transformer")
                }
            }
        }

    return map
}

/**
 * Builds the per-class synthetic member cache from suspend declarations annotated
 * with configured transformer markers.
 */
internal fun SuspendTransformFirTransformer.createCache(
    classSymbol: FirClassSymbol<*>,
    declaredScope: FirClassDeclaredMemberScope?,
    transformerFunctionSymbolMap: Map<Transformer, FirNamedFunctionSymbol>
): Map<Name, Map<FirNamedFunctionSymbol, SyntheticFunData>>? {
    if (declaredScope == null) return null

    val platform = classSymbol.moduleData.platform

    fun check(targetPlatform: TargetPlatform): Boolean {
        return when {
            platform.isJvm() && targetPlatform == TargetPlatform.JVM -> true
            platform.isJs() && targetPlatform == TargetPlatform.JS -> true
            platform.isWasm() && targetPlatform == TargetPlatform.WASM -> true
            platform.isNative() && targetPlatform == TargetPlatform.NATIVE -> true
            platform.isCommon() && targetPlatform == TargetPlatform.COMMON -> true
            else -> false
        }
    }

    // Key -> synthetic fun name
    // Value Map ->
    //      Key: -> origin fun symbol
    //      Values -> FunData
    val map = ConcurrentHashMap<Name, MutableMap<FirNamedFunctionSymbol, SyntheticFunData>>()
//        val transformerFunctionSymbolMap = ConcurrentHashMap<Transformer, FirNamedFunctionSymbol>()

    val platformTransformers = suspendTransformConfiguration.transformers
        .filter { (currentPlatform, _) -> check(currentPlatform) }

    declaredScope.processAllFunctions { func ->
        if (!func.isSuspend) return@processAllFunctions

        val functionName = func.name.asString()

        platformTransformers
            .forEach { (_, transformerList) ->
                for (transformer in transformerList) {
                    val markAnnotation = transformer.markAnnotation

                    val anno = firAnnotation(func, markAnnotation, classSymbol)
                        ?: continue

                    val transformerFunctionSymbol = transformerFunctionSymbolMap[transformer]
                        ?: error("Cannot find transformer function symbol for transformer: $transformer in $platform")

                    // 读不到注解的参数？
                    // 必须使用 anno.getXxxArgument(Name(argument name)),
                    // 使用 argumentMapping.mapping 获取不到结果
                    val annoData = runTransformAnnotationData(anno, markAnnotation, functionName)

                    val syntheticFunNameString = annoData.functionName
                    val syntheticFunName = Name.identifier(syntheticFunNameString)

                    map.computeIfAbsent(syntheticFunName) {
                        ConcurrentHashMap()
                    }[func] = SyntheticFunData(
                        syntheticFunName,
                        annoData,
                        transformer,
                        transformerFunctionSymbol
                    )
                }
            }
    }

    return map
}

/**
 * Reads the configured annotation payload and converts it into synthetic-member metadata.
 */
internal fun SuspendTransformFirTransformer.runTransformAnnotationData(
    annotation: FirAnnotation,
    markAnnotation: MarkAnnotation,
    sourceFunctionName: String
): TransformAnnotationData = TransformAnnotationData.of(
    firSession,
    firAnnotation = annotation,
    annotationBaseNamePropertyName = markAnnotation.baseNameProperty,
    annotationSuffixPropertyName = markAnnotation.suffixProperty,
    annotationAsPropertyPropertyName = markAnnotation.asPropertyProperty,
    annotationMarkNamePropertyName = markAnnotation.markNameProperty?.propertyName,
    defaultBaseName = sourceFunctionName,
    defaultSuffix = markAnnotation.defaultSuffix,
    defaultAsProperty = markAnnotation.defaultAsProperty,
)

/**
 * Finds the effective marker annotation from either the function itself or its containing class.
 */
internal fun SuspendTransformFirTransformer.firAnnotation(
    func: FirNamedFunctionSymbol,
    markAnnotation: MarkAnnotation,
    classSymbol: FirBasedSymbol<*>?
): FirAnnotation? {
    val targetClassId = ClassId(
        markAnnotation.classInfo.packageName.fqn,
        markAnnotation.classInfo.className.fqn,
        markAnnotation.classInfo.local
    )

    return func.resolvedAnnotationsWithArguments.firstOrNull { annotation ->
        annotation.annotationTypeRef.coneTypeOrNull?.classId == targetClassId
    } ?: classSymbol?.resolvedAnnotationsWithArguments?.firstOrNull { annotation ->
        annotation.annotationTypeRef.coneTypeOrNull?.classId == targetClassId
    }
}
