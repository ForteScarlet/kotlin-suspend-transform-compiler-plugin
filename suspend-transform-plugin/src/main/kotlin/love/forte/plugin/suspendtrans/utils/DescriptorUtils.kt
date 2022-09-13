package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType

fun ValueParameterDescriptor.copy(
    containingDeclaration: CallableDescriptor = this.containingDeclaration,
    original: ValueParameterDescriptor? = this.original,
    index: Int = this.index,
    annotations: Annotations = this.annotations,
    name: Name = this.name,
    outType: KotlinType = this.type,
    declaresDefaultValue: Boolean = this.declaresDefaultValue(),
    isCrossinline: Boolean = this.isCrossinline,
    isNoinline: Boolean = this.isNoinline,
    varargElementType: KotlinType? = this.varargElementType,
    source: SourceElement = this.source
): ValueParameterDescriptor {
    return ValueParameterDescriptorImpl(
        containingDeclaration,
        original,
        index,
        annotations,
        name,
        outType,
        declaresDefaultValue,
        isCrossinline,
        isNoinline,
        varargElementType,
        source,
    )
}



