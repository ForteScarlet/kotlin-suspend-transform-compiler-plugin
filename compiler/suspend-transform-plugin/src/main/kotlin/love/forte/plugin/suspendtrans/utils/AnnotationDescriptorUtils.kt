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

package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.configuration.Transformer
import love.forte.plugin.suspendtrans.toJsPromiseAnnotationName
import love.forte.plugin.suspendtrans.toJvmAsyncAnnotationName
import love.forte.plugin.suspendtrans.toJvmBlockingAnnotationName
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe


fun IrBuilderWithScope.irAnnotationConstructor(
    clazz: IrClassSymbol,
): IrConstructorCall {
    return irCall(clazz.constructors.first()).let {
        irConstructorCall(it, it.symbol)
    }
}

fun Iterable<AnnotationDescriptor>.filterNotCompileAnnotations(): List<AnnotationDescriptor> = filterNot {
    val annotationFqNameUnsafe = it.annotationClass?.fqNameUnsafe ?: return@filterNot true
//
    annotationFqNameUnsafe == toJvmAsyncAnnotationName.toUnsafe()
            || annotationFqNameUnsafe == toJvmBlockingAnnotationName.toUnsafe()
            || annotationFqNameUnsafe == toJsPromiseAnnotationName.toUnsafe()
}

//fun Iterable<FirAnnotation>.filterNotCompileAnnotations(session: FirSession): List<FirAnnotation> = filterNot {
//    val annotationFqName = it.fqName(session) ?: return@filterNot true
//    val annotationFqNameUnsafe = it.annotationClass?.fqNameUnsafe ?: return@filterNot true

//    annotationFqName == toJvmAsyncAnnotationName.toUnsafe()
//            || annotationFqName == toJvmBlockingAnnotationName.toUnsafe()
//            || annotationFqName == toJsPromiseAnnotationName.toUnsafe()
//}

data class TransformAnnotationData(
    val baseName: String?,
    val suffix: String?,
    val rawAsProperty: Boolean?,
    val asProperty: Boolean,
    val functionName: String,
    val markName: String?,
) {
    companion object {
        fun of(
            annotationDescriptor: AnnotationDescriptor,
            annotationBaseNamePropertyName: String = "baseName",
            annotationSuffixPropertyName: String = "suffix",
            annotationAsPropertyPropertyName: String = "asProperty",
            annotationMarkNamePropertyName: String? = null,
            defaultBaseName: String,
            defaultSuffix: String,
            defaultAsProperty: Boolean,
        ): TransformAnnotationData {
            val baseName = annotationDescriptor.argumentValue(annotationBaseNamePropertyName)
                ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.stringOnly, null)
                ?.takeIf { it.isNotEmpty() }

            val suffix = annotationDescriptor.argumentValue(annotationSuffixPropertyName)
                ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.stringOnly, null)


            val rawAsProperty = annotationDescriptor.argumentValue(annotationAsPropertyPropertyName)
                ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.booleanOnly, null)

            val functionName = "${baseName ?: defaultBaseName}${suffix ?: defaultSuffix}"

            val markName = if (annotationMarkNamePropertyName != null) {
                annotationDescriptor.argumentValue(annotationMarkNamePropertyName)
                    ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.stringOnly, null)
                    ?.takeIf { it.isNotEmpty() }
            } else {
                null
            }

            return TransformAnnotationData(
                baseName,
                suffix,
                rawAsProperty,
                rawAsProperty ?: defaultAsProperty,
                functionName,
                markName
            )
        }

        fun of(
            session: FirSession,
            firAnnotation: FirAnnotation,
            annotationBaseNamePropertyName: String = "baseName",
            annotationSuffixPropertyName: String = "suffix",
            annotationAsPropertyPropertyName: String = "asProperty",
            annotationMarkNamePropertyName: String? = null,
            defaultBaseName: String,
            defaultSuffix: String,
            defaultAsProperty: Boolean,
        ): TransformAnnotationData {
            val baseName = firAnnotation.getStringArgument0(Name.identifier(annotationBaseNamePropertyName), session)
                ?.takeIf { it.isNotEmpty() }

            val suffix = firAnnotation.getStringArgument0(Name.identifier(annotationSuffixPropertyName), session)

            val rawAsProperty =
                firAnnotation.getBooleanArgument0(Name.identifier(annotationAsPropertyPropertyName), session)

            val functionName = "${baseName ?: defaultBaseName}${suffix ?: defaultSuffix}"

            val markName = if (annotationMarkNamePropertyName != null) {
                firAnnotation.getStringArgument0(Name.identifier(annotationMarkNamePropertyName), session)
                    ?.takeIf { it.isNotEmpty() }
            } else {
                null
            }

            return TransformAnnotationData(
                baseName,
                suffix,
                rawAsProperty,
                rawAsProperty ?: defaultAsProperty,
                functionName,
                markName
            )
        }
    }
}

private fun FirAnnotation.getStringArgument0(
    name: Name,
    session: FirSession
): String? {
    val arg = getStringArgument(name, session)
    if (arg != null) {
        return arg
    }

    // If not found, try to use `findArgumentByName`
    val argByName = findArgumentByName(name, returnFirstWhenNotFound = false)
    return (argByName as? FirLiteralExpression)?.value as? String
}

private fun FirAnnotation.getBooleanArgument0(
    name: Name,
    session: FirSession
): Boolean? {
    val arg = getBooleanArgument(name, session)
    if (arg != null) {
        return arg
    }

    // If not found, try to use `findArgumentByName`
    val argByName = findArgumentByName(name, returnFirstWhenNotFound = false)
    return (argByName as? FirLiteralExpression)?.value as? Boolean
}


fun Transformer.resolveAnnotationData(
    functionDescriptor: FunctionDescriptor,
    containing: DeclarationDescriptor = functionDescriptor.containingDeclaration,
    defaultBaseName: String,
    annotationBaseNamePropertyName: String = this.markAnnotation.baseNameProperty,
    annotationSuffixPropertyName: String = this.markAnnotation.suffixProperty,
    annotationAsPropertyPropertyName: String = this.markAnnotation.asPropertyProperty,
    annotationMarkNamePropertyName: String? = this.markAnnotation.markNameProperty?.propertyName
): TransformAnnotationData? {
    val markAnnotationClassId = markAnnotation.classInfo.toClassId()
    val annotationFqn =
        markAnnotationClassId.asSingleFqName() // .packageFqName.child(markAnnotationClassId.shortClassName)

    val foundAnnotation = functionDescriptor.annotations.findAnnotation(annotationFqn)
        ?: containing.annotations.findAnnotation(annotationFqn)

    return foundAnnotation?.let {
        TransformAnnotationData.of(
            it,
            annotationBaseNamePropertyName,
            annotationSuffixPropertyName,
            annotationAsPropertyPropertyName,
            annotationMarkNamePropertyName,
            defaultBaseName,
            markAnnotation.defaultSuffix,
            markAnnotation.defaultAsProperty,
        )
    }
}

