package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JsAsyncUserData
import love.forte.plugin.suspendtrans.ToJsAsync
import love.forte.plugin.suspendtrans.utils.copy
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJvmBlockingFunctionDescriptorImpl(
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
        initialize(
            originalFunction.extensionReceiverParameter?.copy(this),
            classDescriptor.thisAsReceiverParameter,
            originalFunction.contextReceiverParameters.map { it.copy(this) },
            originalFunction.typeParameters.toList(),
            originalFunction.valueParameters.map { it.copy(containingDeclaration = this) },
            originalFunction.returnType,
            originalFunction.modality,
            originalFunction.visibility,
            mutableMapOf<CallableDescriptor.UserDataKey<*>, Any>(ToJsAsync to JsAsyncUserData(originalFunction))
        )
        this.isSuspend = false
        
    }
}
