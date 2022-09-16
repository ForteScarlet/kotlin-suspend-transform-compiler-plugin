package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JvmAsyncUserData
import love.forte.plugin.suspendtrans.ToJvmAsync
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJvmAsyncPropertyDescriptorImpl(
    sourceClass: ClassDescriptor,
    sourceFunction: SimpleFunctionDescriptor,
    getterAnnotations: Annotations = Annotations.EMPTY,
) : AbstractSuspendTransformProperty<JvmAsyncUserData>(
    sourceClass,
    sourceFunction,
    getterAnnotations,
    ToJvmAsync
)