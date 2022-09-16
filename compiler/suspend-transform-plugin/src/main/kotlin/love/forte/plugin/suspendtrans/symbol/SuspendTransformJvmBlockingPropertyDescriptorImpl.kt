package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JvmBlockingUserData
import love.forte.plugin.suspendtrans.ToJvmBlocking
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJvmBlockingPropertyDescriptorImpl(
    sourceClass: ClassDescriptor,
    sourceFunction: SimpleFunctionDescriptor,
    getterAnnotations: Annotations = Annotations.EMPTY,
) : AbstractSuspendTransformProperty<JvmBlockingUserData>(
    sourceClass,
    sourceFunction,
    getterAnnotations,
    ToJvmBlocking
)