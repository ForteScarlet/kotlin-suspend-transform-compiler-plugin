package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JvmBlockingUserData
import love.forte.plugin.suspendtrans.ToJvmBlocking
import love.forte.plugin.suspendtrans.jsPromiseClassName
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import org.jetbrains.kotlin.descriptors.CallableDescriptor.UserDataKey
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
class SuspendTransformJsPromiseFunctionImpl(
    private val classDescriptor: ClassDescriptor,
    private val originalFunction: SimpleFunctionDescriptor,
    functionName: Name,
    annotations: Annotations = Annotations.EMPTY
) : SimpleFunctionDescriptorImpl(
    classDescriptor,
    null,
    annotations,
    functionName,
    CallableMemberDescriptor.Kind.SYNTHESIZED,
    originalFunction.source
) {
    fun init() {
        val promiseClass = requireNotNull(classDescriptor.module.findClassDescriptor(jsPromiseClassName))
        val promiseType = KotlinTypeFactory.simpleNotNullType(
            TypeAttributes.Empty,
            promiseClass,
            originalFunction.returnType?.let { listOf(TypeProjectionImpl(it)) } ?: emptyList()
        )
        
        
        super.initialize(
            originalFunction.extensionReceiverParameter?.copy(this),
            classDescriptor.thisAsReceiverParameter,
            originalFunction.contextReceiverParameters.map { it.copy(this) },
            originalFunction.typeParameters.toList(),
            originalFunction.valueParameters.map { it.copy(this, it.name, it.index) },
            promiseType,
            originalFunction.modality,
            originalFunction.visibility,
            mutableMapOf<UserDataKey<*>, Any>(ToJvmBlocking to JvmBlockingUserData(originalFunction))
        )
        this.isSuspend = false
        
    }
}
