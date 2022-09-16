package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JvmAsyncUserData
import love.forte.plugin.suspendtrans.ToJvmAsync
import love.forte.plugin.suspendtrans.completableFutureClassName
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isArrayOrNullableArray
import org.jetbrains.kotlin.types.typeUtil.isNothing
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import org.jetbrains.kotlin.types.typeUtil.isUnit

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJvmAsyncFunctionDescriptorImpl(
    private val classDescriptor: ClassDescriptor,
    originFunction: SimpleFunctionDescriptor,
    functionName: String,
    annotationsWithPropertyAnnotations: Pair<Annotations, Annotations>,
) : AbstractSuspendTransformFunctionDescriptor<JvmAsyncUserData>(
    classDescriptor,
    originFunction,
    Name.identifier(functionName),
    annotationsWithPropertyAnnotations.first,
    annotationsWithPropertyAnnotations.second,
    ToJvmAsync to JvmAsyncUserData(originFunction)
) {
    override fun returnType(originReturnType: KotlinType?): KotlinType {
        val futureClass = requireNotNull(classDescriptor.module.findClassDescriptor(completableFutureClassName))
        return KotlinTypeFactory.simpleNotNullType(
            TypeAttributes.Empty,
            futureClass,
            originReturnType?.let {

                val variance = when {
                    it.isUnit() || it.isNothing() -> Variance.INVARIANT
                    it.isPrimitiveNumberType() -> Variance.INVARIANT
                    it.isArrayOrNullableArray() -> Variance.INVARIANT
                    else -> Variance.OUT_VARIANCE
                }
                listOf(TypeProjectionImpl(variance, it))
            } ?: emptyList()
        )
    }

    override fun transformToPropertyInternal(): SuspendTransformJvmAsyncPropertyDescriptorImpl =
        SuspendTransformJvmAsyncPropertyDescriptorImpl(classDescriptor, this, annotations)
}
