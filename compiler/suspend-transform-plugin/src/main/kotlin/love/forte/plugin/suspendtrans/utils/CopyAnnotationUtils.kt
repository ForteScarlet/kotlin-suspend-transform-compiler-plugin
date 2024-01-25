package love.forte.plugin.suspendtrans.utils

import love.forte.plugin.suspendtrans.IncludeAnnotation
import org.jetbrains.kotlin.name.ClassId

data class CopyAnnotationsData(
    val copyFunction: Boolean,
    val excludes: List<ClassId>,
    val includes: List<IncludeAnnotationInfo>
)

data class IncludeAnnotationInfo(
    val classId: ClassId,
    val repeatable: Boolean
)

fun IncludeAnnotation.toInfo(): love.forte.plugin.suspendtrans.utils.IncludeAnnotationInfo {
    return IncludeAnnotationInfo(classInfo.toClassId(), repeatable)
}
