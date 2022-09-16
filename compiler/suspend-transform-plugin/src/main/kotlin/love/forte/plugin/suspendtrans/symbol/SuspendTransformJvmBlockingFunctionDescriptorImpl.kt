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
    private val classDescriptor: ClassDescriptor,
    originFunction: SimpleFunctionDescriptor,
    functionName: String,
    annotationsWithPropertyAnnotations: Pair<Annotations, Annotations>,
    ) : AbstractSuspendTransformFunctionDescriptor<JvmBlockingUserData>(
    classDescriptor,
    originFunction,
    Name.identifier(functionName),
    annotationsWithPropertyAnnotations.first,
    annotationsWithPropertyAnnotations.second,
    ToJvmBlocking to JvmBlockingUserData(originFunction),
) {
    override fun returnType(originReturnType: KotlinType?): KotlinType? = originReturnType
    override fun transformToPropertyInternal(): SuspendTransformJvmBlockingPropertyDescriptorImpl =
        SuspendTransformJvmBlockingPropertyDescriptorImpl(classDescriptor, this, annotations)
}
