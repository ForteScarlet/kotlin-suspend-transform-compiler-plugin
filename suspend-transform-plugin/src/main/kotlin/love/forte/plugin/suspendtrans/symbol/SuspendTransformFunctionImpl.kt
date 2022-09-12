package love.forte.plugin.suspendtrans.symbol

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformFunctionImpl(
    private val classDescriptor: ClassDescriptor,
    functionName: String,
) : SimpleFunctionDescriptorImpl(
    classDescriptor,
    null,
    Annotations.EMPTY,
    Name.identifier(functionName),
    CallableMemberDescriptor.Kind.SYNTHESIZED,
    classDescriptor.source
) {
    fun initialize(
        valueParameters: List<ValueParameterDescriptor> = emptyList(),
    ) {
        super.initialize(
            null,
            classDescriptor.thisAsReceiverParameter,
            emptyList(),
            valueParameters,
            classDescriptor.defaultType,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            
            )
    }
    
}