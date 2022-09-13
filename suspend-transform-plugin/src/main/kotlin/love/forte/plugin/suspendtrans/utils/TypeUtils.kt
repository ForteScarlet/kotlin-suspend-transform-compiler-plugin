package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName


fun ModuleDescriptor.findClassDescriptor(classId: ClassId): ClassDescriptor? {
    return findClassAcrossModuleDependencies(classId)
}


fun ModuleDescriptor.findClassDescriptor(fullName: FqName): ClassDescriptor? {
    return findClassAcrossModuleDependencies(
        ClassId(fullName.parent(), fullName.shortName())
    )
}