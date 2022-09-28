package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.SuspendTransformUserData
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.copy
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType


/**
 *
 * @author ForteScarlet
 */
sealed class AbstractSuspendTransformFunctionDescriptor<D : SuspendTransformUserData>(
    private val classDescriptor: ClassDescriptor,
    private val originFunction: SimpleFunctionDescriptor,
    functionName: Name,
    annotations: Annotations,
    internal val propertyAnnotations: Annotations,
    private val userData: Pair<CallableDescriptor.UserDataKey<D>, D>,
) : SimpleFunctionDescriptorImpl(
    classDescriptor,
    null,
    annotations,
    functionName,
    CallableMemberDescriptor.Kind.SYNTHESIZED,
    originFunction.source
) {

    protected abstract fun returnType(originReturnType: KotlinType?): KotlinType?

    open fun init() {
        initialize(
            originFunction.extensionReceiverParameter?.copy(this),
            classDescriptor.thisAsReceiverParameter,
            originFunction.contextReceiverParameters.map { it.copy(this) },
            originFunction.typeParameters.toList(),
            originFunction.valueParameters.map { it.copy(this) },
            returnType(originFunction.returnType),
            modality(originFunction),
            originFunction.visibility,
            mutableMapOf<CallableDescriptor.UserDataKey<*>, Any>(userData)
        )
        this.isSuspend = false

    }

    protected abstract fun transformToPropertyInternal(): AbstractSuspendTransformProperty<D>

    fun transformToProperty(annotationData: TransformAnnotationData): AbstractSuspendTransformProperty<D> {
        return transformToPropertyInternal().apply { init() }
    }


    protected open fun modality(originFunction: SimpleFunctionDescriptor): Modality {
        if (originFunction.modality == Modality.ABSTRACT) {
            return Modality.OPEN
        }

        return originFunction.modality
    }

}