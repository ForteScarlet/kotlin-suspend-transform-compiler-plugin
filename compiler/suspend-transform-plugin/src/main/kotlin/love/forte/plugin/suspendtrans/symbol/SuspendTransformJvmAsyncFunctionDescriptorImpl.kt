package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JvmAsyncUserData
import love.forte.plugin.suspendtrans.ToJvmAsync
import love.forte.plugin.suspendtrans.completableFutureClassName
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.TypeProjectionImpl

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJvmAsyncFunctionDescriptorImpl(
    private val classDescriptor: ClassDescriptor,
    originFunction: SimpleFunctionDescriptor,
    functionName: String,
    annotations: Annotations = Annotations.EMPTY,
) : AbstractSuspendTransformFunctionDescriptor<JvmAsyncUserData>(
    classDescriptor,
    originFunction,
    Name.identifier(functionName),
    annotations,
    ToJvmAsync to JvmAsyncUserData(originFunction)
) {
    override fun returnType(originReturnType: KotlinType?): KotlinType {
        val futureClass = requireNotNull(classDescriptor.module.findClassDescriptor(completableFutureClassName))
        return KotlinTypeFactory.simpleNotNullType(
            TypeAttributes.Empty,
            futureClass,
            originReturnType?.let { listOf(TypeProjectionImpl(it)) } ?: emptyList()
        )
    }
}
