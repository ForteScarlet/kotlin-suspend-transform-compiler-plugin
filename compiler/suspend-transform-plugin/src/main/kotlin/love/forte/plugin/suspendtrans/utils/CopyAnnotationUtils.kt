package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.configuration.ClassInfo
import love.forte.plugin.suspendtrans.configuration.IncludeAnnotation
import org.jetbrains.kotlin.name.ClassId

data class IncludeAnnotationInfo(
    val classId: ClassId,
    val classInfo: ClassInfo,
    val repeatable: Boolean,
    val includeProperty: Boolean,
)

fun IncludeAnnotation.toInfo(): IncludeAnnotationInfo {
    return IncludeAnnotationInfo(classInfo.toClassId(), classInfo, repeatable, includeProperty)
}
