package love.forte.plugin.suspendtrans.symbol

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations


/**
 *
 * @author ForteScarlet
 */
class SimpleSuspendTransformPropertyDescriptor(
    sourceClass: ClassDescriptor,
    sourceFunction: SimpleFunctionDescriptor,
    getterAnnotations: Annotations = Annotations.EMPTY,
) : AbstractSuspendTransformProperty(
    sourceClass,
    sourceFunction,
    getterAnnotations
)
