package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JsAsyncUserData
import love.forte.plugin.suspendtrans.ToJsAsync
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJsAsyncPropertyDescriptorImpl(
    sourceClass: ClassDescriptor,
    sourceFunction: SimpleFunctionDescriptor,
    getterAnnotations: Annotations = Annotations.EMPTY,
) : AbstractSuspendTransformProperty<JsAsyncUserData>(
    sourceClass,
    sourceFunction,
    getterAnnotations,
    ToJsAsync
)