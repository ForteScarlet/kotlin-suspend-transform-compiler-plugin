package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.SuspendTransformConfiguration


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformGradleExtension : SuspendTransformConfiguration() {

    /**
     * 是否增加 `love.forte.plugin.suspend-transform:suspend-transform-annotation` 的运行时。
     */
    var includeAnnotation: Boolean = true

    var annotationDependencyVersion: String = SuspendTransPluginConstants.ANNOTATION_VERSION

    /**
     * 当 [includeAnnotation] 为 true 时，配置runtime环境的依赖方式。默认为 `compileOnly` （在JVM中） 。
     */
    var annotationConfigurationName: String = "compileOnly"

    /**
     * 是否增加 `love.forte.plugin.suspend-transform:suspend-transform-runtime` 的运行时。
     */
    var includeRuntime: Boolean = true

    var runtimeDependencyVersion: String = SuspendTransPluginConstants.RUNTIME_VERSION

    /**
     * 当 [includeRuntime] 为 true 时，配置runtime环境的依赖方式。默认为 `implementation` （在JVM中）。
     */
    var runtimeConfigurationName: String = "implementation"

    /**
     * 将runtime环境作为 `api` 的方式进行配置（在JVM中）。
     */
    fun runtimeAsApi() {
        runtimeConfigurationName = "api"
    }

    override fun toString(): String {
        return "SuspendTransformGradleExtension(includeAnnotation=$includeAnnotation, annotationConfigurationName='$annotationConfigurationName', includeRuntime=$includeRuntime, runtimeConfigurationName='$runtimeConfigurationName')"
    }


}
