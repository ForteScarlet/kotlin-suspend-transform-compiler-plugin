package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.*
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe


fun AnnotationDescriptor?.functionName(
    baseNamePropertyName: String = "baseName",
    suffixPropertyName: String = "suffix",
    defaultBaseName: String, defaultSuffix: String,
): String {
    if (this == null) return "$defaultBaseName$defaultSuffix"

    val visitor = object : AbstractNullableAnnotationArgumentVoidDataVisitor<String>() {
        override fun visitStringValue(value: String): String = value
    }

    val baseName = argumentValue(baseNamePropertyName)?.accept(visitor, null)
    val suffix = argumentValue(suffixPropertyName)?.accept(visitor, null)

    return (baseName ?: defaultBaseName) + (suffix ?: defaultSuffix)
}

/**
 * Get the `@Generated` annotation.
 */
val IrPluginContext.generatedAnnotation get() = referenceClass(generatedAnnotationName)!!


fun IrBuilderWithScope.irAnnotationConstructor(
    clazz: IrClassSymbol,
): IrConstructorCall {
    return irCall(clazz.constructors.first()).let {
        irConstructorCall(it, it.symbol)
    }
}

fun List<IrConstructorCall>.filterNotCompileAnnotations(): List<IrConstructorCall> = filterNot {
    it.type.isClassType(toJvmAsyncAnnotationName.toUnsafe()) || it.type.isClassType(toJvmBlockingAnnotationName.toUnsafe()) || it.type.isClassType(
        toJsPromiseAnnotationName.toUnsafe()
    )
}

private fun IrDeclarationWithName.hasEqualFqName(fqName: FqName): Boolean =
    name == fqName.shortName() && when (val parent = parent) {
        is IrPackageFragment -> parent.fqName == fqName.parent()
        is IrDeclarationWithName -> parent.hasEqualFqName(fqName.parent())
        else -> false
    }

fun Iterable<AnnotationDescriptor>.filterNotCompileAnnotations(): List<AnnotationDescriptor> = filterNot {
    val annotationFqNameUnsafe = it.annotationClass?.fqNameUnsafe ?: return@filterNot true

    annotationFqNameUnsafe == toJvmAsyncAnnotationName.toUnsafe()
            || annotationFqNameUnsafe == toJvmBlockingAnnotationName.toUnsafe()
            || annotationFqNameUnsafe == toJsPromiseAnnotationName.toUnsafe()
}

data class TransformAnnotationData(
    val annotationDescriptor: AnnotationDescriptor,
    val annotationBaseNamePropertyName: String = "baseName",
    val annotationSuffixPropertyName: String = "suffix",
    val annotationAsPropertyPropertyName: String = "asProperty",
    val defaultBaseName: String,
    val defaultSuffix: String,
) {
    val baseName: String? = annotationDescriptor.argumentValue(annotationBaseNamePropertyName)
        ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.stringOnly, null)
    val suffix: String? = annotationDescriptor.argumentValue(annotationSuffixPropertyName)
        ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.stringOnly, null)
    val asProperty: Boolean? = annotationDescriptor.argumentValue(annotationAsPropertyPropertyName)
        ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.booleanOnly, null)
    val functionName: String = "${baseName ?: defaultBaseName}${suffix ?: defaultSuffix}"
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

fun Annotations.resolveToTransformAnnotations(
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
            TransformAnnotationData(
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