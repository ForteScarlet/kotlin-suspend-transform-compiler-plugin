package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

object SuspendTransformUserDataKey : CallableDescriptor.UserDataKey<SuspendTransformUserData>

data class SuspendTransformUserData(
    val originFunction: SimpleFunctionDescriptor,
    val asProperty: Boolean,
    val transformer: Transformer
)
