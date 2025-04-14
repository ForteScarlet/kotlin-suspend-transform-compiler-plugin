package love.forte.plugin.suspendtrans.gradle

const val USE_NEW_EXTENSION = "Use `love.forte.plugin.suspendtrans.gradle.SuspendTransformPluginExtension` " +
        "(`suspendTransformPlugin { ... }`)"

/**
 *
 * @author ForteScarlet
 */
@Suppress("DEPRECATION")
@Deprecated(
    USE_NEW_EXTENSION,
    ReplaceWith(
        "SuspendTransformPluginExtension",
        "love.forte.plugin.suspendtrans.gradle.SuspendTransformPluginExtension"
    )
)
open class SuspendTransformGradleExtension : love.forte.plugin.suspendtrans.SuspendTransformConfiguration() {
    @Deprecated("Please use the " +
            "`love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration` " +
            "(`suspendTransformPlugin { ... }`) instead.")
    override var enabled: Boolean = false

    /**
     * 是否增加 `love.forte.plugin.suspend-transform:suspend-transform-annotation` 的运行时。
     */
    @Deprecated(USE_NEW_EXTENSION)
    open var includeAnnotation: Boolean = true

    @Deprecated(USE_NEW_EXTENSION)
    open var annotationDependencyVersion: String = SuspendTransPluginConstants.ANNOTATION_VERSION

    /**
     * 当 [includeAnnotation] 为 true 时，配置runtime环境的依赖方式。默认为 `compileOnly` （在JVM中） 。
     */
    @Deprecated(USE_NEW_EXTENSION)
    open var annotationConfigurationName: String = "compileOnly"

    /**
     * 是否增加 `love.forte.plugin.suspend-transform:suspend-transform-runtime` 的运行时。
     */
    @Deprecated(USE_NEW_EXTENSION)
    open var includeRuntime: Boolean = true

    @Deprecated(USE_NEW_EXTENSION)
    open var runtimeDependencyVersion: String = SuspendTransPluginConstants.RUNTIME_VERSION

    /**
     * 当 [includeRuntime] 为 true 时，配置runtime环境的依赖方式。默认为 `implementation` （在JVM中）。
     */
    @Deprecated(USE_NEW_EXTENSION)
    open var runtimeConfigurationName: String = "implementation"

    /**
     * 将runtime环境作为 `api` 的方式进行配置（在JVM中）。
     */
    @Deprecated(USE_NEW_EXTENSION)
    open fun runtimeAsApi() {
        runtimeConfigurationName = "api"
    }

    override fun toString(): String {
        return "SuspendTransformGradleExtension(includeAnnotation=$includeAnnotation, annotationConfigurationName='$annotationConfigurationName', includeRuntime=$includeRuntime, runtimeConfigurationName='$runtimeConfigurationName')"
    }


}
