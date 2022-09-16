package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.JsAsyncUserData
import love.forte.plugin.suspendtrans.ToJsAsync
import love.forte.plugin.suspendtrans.jsPromiseClassName
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.TypeProjectionImpl

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformJsPromiseFunctionImpl(
    private val classDescriptor: ClassDescriptor,
    originalFunction: SimpleFunctionDescriptor,
    functionName: String,
    annotations: Annotations = Annotations.EMPTY,
) : AbstractSuspendTransformFunctionDescriptor<JsAsyncUserData>(
    classDescriptor,
    originalFunction,
    Name.identifier(functionName),
    annotations,
    ToJsAsync to JsAsyncUserData(originalFunction)
) {
    override fun returnType(originReturnType: KotlinType?): KotlinType {
        val promiseClass = requireNotNull(classDescriptor.module.findClassDescriptor(jsPromiseClassName))
        return KotlinTypeFactory.simpleNotNullType(
            TypeAttributes.Empty,
            promiseClass,
            originReturnType?.let { listOf(TypeProjectionImpl(it)) } ?: emptyList()
        )
    }
}
