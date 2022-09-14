package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor


data class JvmBlockingUserData(val originFunction: SimpleFunctionDescriptor)
object ToJvmBlocking : CallableDescriptor.UserDataKey<JvmBlockingUserData>

data class JvmAsyncUserData(val originFunction: SimpleFunctionDescriptor)
object ToJvmAsync : CallableDescriptor.UserDataKey<JvmAsyncUserData>


data class JsAsyncUserData(val originFunction: SimpleFunctionDescriptor)
object ToJsAsync : CallableDescriptor.UserDataKey<JsAsyncUserData>