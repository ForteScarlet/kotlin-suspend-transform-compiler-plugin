package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.SuspendTransformUserDataKey
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.CallableDescriptor.UserDataKey
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing

/**
 *
 * @author ForteScarlet
 */
abstract class AbstractSuspendTransformProperty(
    private val sourceClass: ClassDescriptor,
    private val sourceFunction: SimpleFunctionDescriptor,
    private val getterAnnotations: Annotations = sourceFunction.annotations, // TODO?
) : PropertyDescriptorImpl(
    sourceClass,
    null,
    getterAnnotations,
    sourceFunction.modality,
    sourceFunction.visibility,
    false,
    sourceFunction.name,
    CallableMemberDescriptor.Kind.DECLARATION,
    sourceFunction.source,
    false,
    false,
    sourceFunction.isExpect,
    sourceFunction.isActual,
    sourceFunction.isExternal,
    false
) {
    private val userDataValue = sourceFunction.getUserData(SuspendTransformUserDataKey)

    @Suppress("UNCHECKED_CAST")
    override fun <V : Any?> getUserData(key: UserDataKey<V>?): V? {
        if (key == SuspendTransformUserDataKey) {
            return userDataValue as? V?
        }

        return super.getUserData(key)
    }

    fun init() {
        this.setType(
            sourceFunction.returnTypeOrNothing,
            sourceFunction.typeParameters,
            sourceFunction.dispatchReceiverParameter,
            sourceFunction.extensionReceiverParameter,
            sourceFunction.contextReceiverParameters
        )
        this.initialize(
            PropertyGetterDescriptorImpl(
                this,
                getterAnnotations,
                this.modality,
                this.visibility,
                sourceClass.kind.isInterface,
                this.isExternal,
                false,
                CallableMemberDescriptor.Kind.DECLARATION,
                null,
                this.source
            ).apply {
                initialize(sourceFunction.returnType)
            },
            null
        )
    }
}

private fun modality(originFunction: SimpleFunctionDescriptor): Modality {
    if (originFunction.modality == Modality.ABSTRACT) {
        return Modality.OPEN
    }

    return originFunction.modality
}
