package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.configuration.IncludeAnnotation
import org.jetbrains.kotlin.name.ClassId

data class CopyAnnotationsData(
    val copyFunction: Boolean,
    val copyProperty: Boolean,
    val excludes: List<ClassId>,
    val includes: List<IncludeAnnotationInfo>
)

data class IncludeAnnotationInfo(
    val classId: ClassId,
    val repeatable: Boolean,
    val includeProperty: Boolean,
)

fun IncludeAnnotation.toInfo(): IncludeAnnotationInfo {
    return IncludeAnnotationInfo(classInfo.toClassId(), repeatable, includeProperty)
}
