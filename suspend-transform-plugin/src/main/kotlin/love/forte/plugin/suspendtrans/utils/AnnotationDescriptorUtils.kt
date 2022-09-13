package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.generatedAnnotationName
import love.forte.plugin.suspendtrans.toJsPromiseAnnotationName
import love.forte.plugin.suspendtrans.toJvmAsyncAnnotationName
import love.forte.plugin.suspendtrans.toJvmBlockingAnnotationName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.resolve.annotations.argumentValue


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