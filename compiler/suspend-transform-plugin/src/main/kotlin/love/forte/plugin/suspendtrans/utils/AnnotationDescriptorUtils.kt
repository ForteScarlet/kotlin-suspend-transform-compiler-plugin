package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.*
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.irConstructorCall
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

    annotationFqNameUnsafe == toJvmAsyncAnnotationName.toUnsafe()
            || annotationFqNameUnsafe == toJvmBlockingAnnotationName.toUnsafe()
            || annotationFqNameUnsafe == toJsPromiseAnnotationName.toUnsafe()
}

data class TransformAnnotationData(
    val annotationDescriptor: AnnotationDescriptor,
    val baseName: String?,
    val suffix: String?,
    val asProperty: Boolean?,
    val functionName: String
) {
    companion object {
        fun of(
            annotationDescriptor: AnnotationDescriptor,
            annotationBaseNamePropertyName: String = "baseName",
            annotationSuffixPropertyName: String = "suffix",
            annotationAsPropertyPropertyName: String = "asProperty",
            defaultBaseName: String,
            defaultSuffix: String,
        ): TransformAnnotationData {
            val baseName = annotationDescriptor.argumentValue(annotationBaseNamePropertyName)
                ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.stringOnly, null)
                ?.takeIf { it.isNotEmpty() }
            val suffix = annotationDescriptor.argumentValue(annotationSuffixPropertyName)
                ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.stringOnly, null)
                ?.takeIf { it.isNotEmpty() }
            val asProperty = annotationDescriptor.argumentValue(annotationAsPropertyPropertyName)
                ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.booleanOnly, null)
            val functionName = "${baseName ?: defaultBaseName}${suffix ?: defaultSuffix}"

            return TransformAnnotationData(annotationDescriptor, baseName, suffix, asProperty, functionName)
        }
    }


}

open class FunctionTransformAnnotations(
    val jvmBlockingAnnotationData: TransformAnnotationData?,
    val jvmAsyncAnnotationData: TransformAnnotationData?,
    val jsAsyncAnnotationData: TransformAnnotationData?,
) {
    open val isEmpty: Boolean get() = jvmBlockingAnnotationData == null && jvmAsyncAnnotationData == null && jsAsyncAnnotationData == null

    object Empty : FunctionTransformAnnotations(null, null, null) {
        override val isEmpty: Boolean
            get() = true
    }
}


fun FunctionDescriptor.resolveToTransformAnnotations(
    containing: DeclarationDescriptor = this.containingDeclaration,
    configuration: SuspendTransformConfiguration
): FunctionTransformAnnotations {
    val baseName = this.name.identifier
    val containingAnnotations = containing.annotations.resolveToTransformAnnotations(configuration, baseName)
    val functionAnnotations = this.annotations.resolveToTransformAnnotations(configuration, baseName)

    return containingAnnotations + functionAnnotations
}

/*
 * 后者优先。
 */
private operator fun FunctionTransformAnnotations.plus(other: FunctionTransformAnnotations): FunctionTransformAnnotations {
    if (other.isEmpty) return this

    return FunctionTransformAnnotations(
        jvmBlockingAnnotationData + other.jvmBlockingAnnotationData,
        jvmAsyncAnnotationData + other.jvmAsyncAnnotationData,
        jsAsyncAnnotationData + other.jsAsyncAnnotationData,
    )
}

/*
 * 后者优先。
 */
private operator fun TransformAnnotationData?.plus(other: TransformAnnotationData?): TransformAnnotationData? {
    println("class annotation data:    $this")
    println("function annotation data: $other")
    if (this == null && other == null) return null
    if (other == null) return this
    if (this == null) return other

    val baseName = other.baseName ?: this.baseName
    val suffix = other.suffix ?: this.suffix
    val asProperty = other.asProperty?.takeIf { it } ?: this.asProperty

    val functionName = if (baseName == null) other.functionName else buildString {
        append(baseName)
        suffix?.also(::append)
    }

    return TransformAnnotationData(
        annotationDescriptor = other.annotationDescriptor,
        baseName = baseName,
        suffix = suffix,
        asProperty = asProperty,
        functionName = functionName
    )

}


private fun Annotations.resolveToTransformAnnotations(
    configuration: SuspendTransformConfiguration,
    functionBaseName: String
): FunctionTransformAnnotations {
    fun SuspendTransformConfiguration.MarkAnnotation.resolve(
        defaultBaseName: String,
        defaultSuffix: String,
        annotationBaseNamePropertyName: String = this.baseNameProperty,
        annotationSuffixPropertyName: String = this.suffixProperty,
        annotationAsPropertyPropertyName: String = this.asPropertyProperty,
    ): TransformAnnotationData? {
        return findAnnotation(this.annotationName.fqn)?.let {
            TransformAnnotationData.of(
                it,
                annotationBaseNamePropertyName,
                annotationSuffixPropertyName,
                annotationAsPropertyPropertyName,
                defaultBaseName,
                defaultSuffix
            )
        }
    }

    val jvmBlockingMarkAnnotation = configuration.jvm.jvmBlockingMarkAnnotation
    val jvmAsyncMarkAnnotation = configuration.jvm.jvmAsyncMarkAnnotation
    val jsAsyncMarkAnnotation = configuration.js.jsPromiseMarkAnnotation

    val jvmBlocking =
        jvmBlockingMarkAnnotation.resolve(
            functionBaseName,
            "Blocking"
        )
    val jvmAsync = jvmAsyncMarkAnnotation.resolve(
        functionBaseName,
        "Async"
    )
    val jsAsync = jsAsyncMarkAnnotation.resolve(
        functionBaseName,
        "Async"
    )

    if (jvmBlocking == null && jvmAsync == null && jsAsync == null) return FunctionTransformAnnotations.Empty
    return FunctionTransformAnnotations(jvmBlocking, jvmAsync, jsAsync)
}