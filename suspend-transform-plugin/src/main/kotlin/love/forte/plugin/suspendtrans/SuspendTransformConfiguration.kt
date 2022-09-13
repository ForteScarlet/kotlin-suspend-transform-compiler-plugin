package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.descriptors.CallableDescriptor.UserDataKey
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformConfiguration {
    
    
    /**
     * Jvm platform config
     */
    open class Jvm
}

data class JvmBlockingUserData(val originFunction: SimpleFunctionDescriptor)
object ToJvmBlocking : UserDataKey<JvmBlockingUserData>

data class JvmAsyncUserData(val originFunction: SimpleFunctionDescriptor)
object ToJvmAsync : UserDataKey<JvmAsyncUserData>


data class JsAsyncUserData(val originFunction: SimpleFunctionDescriptor)
object ToJsAsync : UserDataKey<JsAsyncUserData>
