package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.SuspendTransformUserData
import love.forte.plugin.suspendtrans.utils.copy
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType


/**
 *
 * @author ForteScarlet
 */
abstract class AbstractSuspendTransformFunctionDescriptor<D : SuspendTransformUserData>(
    private val classDescriptor: ClassDescriptor,
    private val originFunction: SimpleFunctionDescriptor,
    functionName: Name,
    annotations: Annotations = Annotations.EMPTY,
    private val userData: Pair<CallableDescriptor.UserDataKey<D>, D>,
) : SimpleFunctionDescriptorImpl(
    classDescriptor,
    null,
    annotations,
    functionName,
    CallableMemberDescriptor.Kind.DECLARATION,
    originFunction.source
) {

    protected abstract fun returnType(originReturnType: KotlinType?): KotlinType?

    open fun init() {
        initialize(
            originFunction.extensionReceiverParameter?.copy(this),
            classDescriptor.thisAsReceiverParameter,
            originFunction.contextReceiverParameters.map { it.copy(this) },
            originFunction.typeParameters.toList(),
            originFunction.valueParameters.map { it.copy(containingDeclaration = this) },
            returnType(originFunction.returnType),
            originFunction.modality,
            originFunction.visibility,
            mutableMapOf<CallableDescriptor.UserDataKey<*>, Any>(userData)
        )
        this.isSuspend = false

    }
}