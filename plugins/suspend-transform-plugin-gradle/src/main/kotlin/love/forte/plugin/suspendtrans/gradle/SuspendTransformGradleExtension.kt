package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.SuspendTransformConfiguration


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformGradleExtension : SuspendTransformConfiguration() {
    /**
     * 是否增加 `love.forte.plugin.suspend-transform:suspend-transform-runtime` 的运行时。
     */
    var includeRuntime: Boolean = true

    /**
     * 当 [includeRuntime] 为 true 时，配置runtime环境的依赖方式。默认为 `implementation`。
     */
    var runtimeConfigurationName: String = "implementation"

    /**
     * 将runtime环境作为 `api` 的方式进行配置。
     */
    fun runtimeAsApi() {
        runtimeConfigurationName = "api"
    }

    override fun toString(): String {
        return "SuspendTransformGradleExtension(includeRuntime=$includeRuntime, runtimeConfigurationName='$runtimeConfigurationName', super=${super.toString()})"
    }


}
