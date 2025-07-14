package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import love.forte.plugin.suspendtrans.configuration.TargetPlatform as PluginTargetPlatform

internal fun checkPlatform(
    platform: TargetPlatform?,
    targetPlatform: PluginTargetPlatform
): Boolean = when {
    platform.isJvm() && targetPlatform == PluginTargetPlatform.JVM -> true
    platform.isJs() && targetPlatform == PluginTargetPlatform.JS -> true
    platform.isWasm() && targetPlatform == PluginTargetPlatform.WASM -> true
    platform.isNative() && targetPlatform == PluginTargetPlatform.NATIVE -> true
    platform.isCommon() && targetPlatform == PluginTargetPlatform.COMMON -> true
    else -> false
}