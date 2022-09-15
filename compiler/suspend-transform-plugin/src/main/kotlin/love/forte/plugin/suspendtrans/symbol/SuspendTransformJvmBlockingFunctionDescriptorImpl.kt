package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JvmBlockingUserData
import love.forte.plugin.suspendtrans.ToJvmBlocking
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJvmBlockingFunctionDescriptorImpl(
    classDescriptor: ClassDescriptor,
    originFunction: SimpleFunctionDescriptor,
    functionName: Name,
    annotations: Annotations = Annotations.EMPTY
) : AbstractSuspendTransformFunctionDescriptor<JvmBlockingUserData>(
    classDescriptor,
    originFunction,
    functionName,
    annotations,
    ToJvmBlocking to JvmBlockingUserData(originFunction),
) {
    override fun returnType(originReturnType: KotlinType?): KotlinType? = originReturnType
}
