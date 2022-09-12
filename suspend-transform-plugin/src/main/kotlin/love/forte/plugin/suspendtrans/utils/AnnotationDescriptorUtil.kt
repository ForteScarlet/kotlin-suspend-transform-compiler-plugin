package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.AbstractNullableAnnotationArgumentVoidDataVisitor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
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