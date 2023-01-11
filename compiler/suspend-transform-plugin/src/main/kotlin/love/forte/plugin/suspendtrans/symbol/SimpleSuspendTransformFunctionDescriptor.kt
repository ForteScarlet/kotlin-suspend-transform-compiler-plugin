package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.SuspendTransformUserData
import love.forte.plugin.suspendtrans.Transformer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name

/**
 *
 * @author ForteScarlet
 */
class SimpleSuspendTransformFunctionDescriptor(
    classDescriptor: ClassDescriptor,
    originFunction: SimpleFunctionDescriptor,
    functionName: String,
    annotationsWithPropertyAnnotations: Pair<Annotations, Annotations>,
    userData: SuspendTransformUserData,
    transformer: Transformer
) : AbstractSuspendTransformFunctionDescriptor(
    classDescriptor,
    originFunction,
    Name.identifier(functionName),
    annotationsWithPropertyAnnotations.first,
    annotationsWithPropertyAnnotations.second,
    userData,
    transformer
)
