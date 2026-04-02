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
import love.forte.plugin.suspendtrans.utils.toClassId
import love.forte.plugin.suspendtrans.utils.toInfo
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.utils.keysToMap

/**
 * Resolves the return type of a generated synthetic member, wrapping the original
 * type when the transformer requests it.
 */
internal fun SuspendTransformFirTransformer.resolveReturnType(
    transformer: Transformer,
    returnTypeRef: FirTypeRef
): FirTypeRef {
    val resultConeType = resolveReturnConeType(transformer, returnTypeRef)

    return if (resultConeType is ConeErrorType) {
        buildErrorTypeRef {
            diagnostic = resultConeType.diagnostic
            coneType = resultConeType
        }
    } else {
        buildResolvedTypeRef {
            coneType = resultConeType
        }
    }
}

/**
 * Produces the effective cone type of a generated member after transformer wrapping.
 */
internal fun SuspendTransformFirTransformer.resolveReturnConeType(
    transformer: Transformer,
    returnTypeRef: FirTypeRef
) = transformer.transformReturnType
    ?.let { returnType ->
        var typeArguments: Array<ConeTypeProjection> = emptyArray()

        if (transformer.transformReturnTypeGeneric) {
            typeArguments = arrayOf(ConeKotlinTypeProjectionOut(returnTypeRef.coneType))
        }

        returnType.toClassId().createConeType(
            session = firSession,
            typeArguments = typeArguments,
            nullable = returnType.nullable
        )
    }
    ?: returnTypeRef.coneType

/**
 * Copies and augments annotations for generated functions, generated properties,
 * and the original source function.
 *
 * @return function annotations `to` property annotations.
 */
internal fun SuspendTransformFirTransformer.copyAnnotations(
    original: FirNamedFunction,
    syntheticFunData: SyntheticFunData,
): CopyAnnotations {
    val transformer = syntheticFunData.transformer

    val originalAnnotationClassIdMap: Map<FirAnnotation, ClassId?> =
        original.annotations.keysToMap { it.resolvedType.classId }

    val copyFunction = transformer.copyAnnotationsToSyntheticFunction
    val copyProperty = transformer.copyAnnotationsToSyntheticProperty
    val excludes = transformer.copyAnnotationExcludes.map { it.toClassId() }
    val includes = transformer.syntheticFunctionIncludeAnnotations.map { it.toInfo() }
    val markNameProperty = transformer.markAnnotation.markNameProperty

    val functionAnnotationList = buildList<FirAnnotation> {
        if (copyFunction) {
            val notCompileAnnotationsCopied = originalAnnotationClassIdMap.filterNot { (_, annotationClassId) ->
                if (annotationClassId == null) return@filterNot true
                excludes.any { ex -> annotationClassId == ex }
            }.keys

            /*
             * Create a new annotation based the annotation from the original function.
             * It will be crashed with `IllegalArgumentException: Failed requirement`
             * when using the `notCompileAnnotationsCopied` directly
             * if there have some arguments with type `KClass`,
             * e.g. `annotation class OneAnnotation(val target: KClass<*>)` or `kotlin.OptIn`.
             *
             * See https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues/56
             */
            val copied = notCompileAnnotationsCopied.map { a ->
                buildAnnotation {
                    annotationTypeRef = buildResolvedTypeRef {
                        coneType = a.resolvedType
                    }
                    this.typeArguments.addAll(a.typeArguments)
                    this.argumentMapping = buildAnnotationArgumentMapping {
                        this.source = a.source
                        this.mapping.putAll(a.argumentMapping.mapping)
                    }
                }
            }

            addAll(copied)
        }

        // add includes
        includes.forEach { include ->
            val classId = include.classId
            val includeAnnotation = buildAnnotation {
                argumentMapping = buildAnnotationArgumentMapping()
                annotationTypeRef = buildResolvedTypeRef {
                    coneType = classId.createConeType(firSession)
                }
            }
            add(includeAnnotation)
        }

        if (markNameProperty != null) {
            // Add name marker annotation if it's possible
            val markName = syntheticFunData.annotationData.markName
            if (markName != null) {
                // Find the marker annotation, e.g., JvmName
                val markNameAnnotation = buildAnnotation {
                    argumentMapping = buildAnnotationArgumentMapping {
                        // org.jetbrains.kotlin.fir.java.JavaUtilsKt
                        val markNameArgument = buildLiteralExpression(
                            source = null,
                            kind = ConstantValueKind.String,
                            value = markName,
                            setType = true
                        )

                        val annotationMarkNamePropertyName = markNameProperty.annotationMarkNamePropertyName
                        mapping[Name.identifier(annotationMarkNamePropertyName)] = markNameArgument
                    }
                    val markNameAnnotationClassId = markNameProperty.annotation.toClassId()
                    annotationTypeRef = buildResolvedTypeRef {
                        coneType = markNameAnnotationClassId.createConeType(firSession)
                    }
                }

                add(markNameAnnotation)
            }
        }
    }

    val propertyAnnotationList = buildList<FirAnnotation> {
        if (copyProperty) {
            val notCompileAnnotationsCopied = originalAnnotationClassIdMap.filterNot { (_, annotationClassId) ->
                if (annotationClassId == null) return@filterNot true
                excludes.any { ex -> annotationClassId == ex }
            }.keys

            addAll(notCompileAnnotationsCopied)
        }

        // add includes
        includes
            .filter { it.includeProperty }
            .forEach { include ->
                val classId = include.classId
                val includeAnnotation = buildAnnotation {
                    argumentMapping = buildAnnotationArgumentMapping()
                    annotationTypeRef = buildResolvedTypeRef {
                        coneType = classId.createConeType(firSession)
                    }
                }
                add(includeAnnotation)
            }
    }

    // original annotations

    val infos = transformer.originFunctionIncludeAnnotations.map { it.toInfo() }

    val includeToOriginals: List<FirAnnotation> = infos
        .mapNotNull { (classId, repeatable, _) ->
            if (!repeatable) {
                // 不能是已经存在的
                if (originalAnnotationClassIdMap.values.any { it == classId }) {
                    return@mapNotNull null
                }
            }

            buildAnnotation {
                argumentMapping = buildAnnotationArgumentMapping()
                annotationTypeRef = buildResolvedTypeRef {
                    coneType = classId.createConeType(firSession)
                }
            }
        }

    return CopyAnnotations(functionAnnotationList, propertyAnnotationList, includeToOriginals)
}
