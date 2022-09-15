package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

interface SuspendTransformUserData {
    val originFunction: SimpleFunctionDescriptor
    val asProperty: Boolean
}

data class JvmBlockingUserData(
    override val originFunction: SimpleFunctionDescriptor,
    override val asProperty: Boolean = false
) : SuspendTransformUserData

object ToJvmBlocking : CallableDescriptor.UserDataKey<JvmBlockingUserData>

data class JvmAsyncUserData(
    override val originFunction: SimpleFunctionDescriptor,
    override val asProperty: Boolean = false
) : SuspendTransformUserData

object ToJvmAsync : CallableDescriptor.UserDataKey<JvmAsyncUserData>


data class JsAsyncUserData(
    override val originFunction: SimpleFunctionDescriptor,
    override val asProperty: Boolean = false
) : SuspendTransformUserData

object ToJsAsync : CallableDescriptor.UserDataKey<JsAsyncUserData>