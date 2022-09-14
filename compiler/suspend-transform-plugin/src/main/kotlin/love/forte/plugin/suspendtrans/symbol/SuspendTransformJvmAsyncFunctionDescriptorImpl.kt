package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JvmAsyncUserData
import love.forte.plugin.suspendtrans.ToJvmAsync
import love.forte.plugin.suspendtrans.completableFutureClassName
import love.forte.plugin.suspendtrans.utils.copy
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.TypeProjectionImpl

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJvmAsyncFunctionDescriptorImpl(
    private val classDescriptor: ClassDescriptor,
    private val originalFunction: SimpleFunctionDescriptor,
    functionName: Name,
) : SimpleFunctionDescriptorImpl(
    classDescriptor,
    null,
    Annotations.EMPTY,
    functionName,
    CallableMemberDescriptor.Kind.SYNTHESIZED,
    originalFunction.source
) {
    fun init() {
        val futureClass = requireNotNull(classDescriptor.module.findClassDescriptor(completableFutureClassName))
        val futureType = KotlinTypeFactory.simpleNotNullType(
            TypeAttributes.Empty,
            futureClass,
            originalFunction.returnType?.let { listOf(TypeProjectionImpl(it)) } ?: emptyList()
        )
        
        initialize(
            originalFunction.extensionReceiverParameter?.copy(this),
            classDescriptor.thisAsReceiverParameter,
            originalFunction.contextReceiverParameters.map { it.copy(this) },
            originalFunction.typeParameters.toList(),
            originalFunction.valueParameters.map { it.copy(containingDeclaration = this) },
            futureType,
            originalFunction.modality,
            originalFunction.visibility,
            mutableMapOf<CallableDescriptor.UserDataKey<*>, Any>(ToJvmAsync to JvmAsyncUserData(originalFunction))
        )
        this.isSuspend = false
        
    }
}
