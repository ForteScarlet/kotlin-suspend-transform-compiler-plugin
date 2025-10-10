/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package love.forte.plugin.suspendtrans.gradle

const val USE_NEW_EXTENSION = "Use the new extension " +
        "`love.forte.plugin.suspendtrans.gradle.SuspendTransformPluginExtension` " +
        "(`suspendTransformPlugin { ... }`)"

/**
 *
 * @author ForteScarlet
 */
@Suppress("DEPRECATION")
@Deprecated(
    message = USE_NEW_EXTENSION,
    replaceWith = ReplaceWith(
        "SuspendTransformPluginExtension",
        "love.forte.plugin.suspendtrans.gradle.SuspendTransformPluginExtension"
    )
)
open class SuspendTransformGradleExtension : love.forte.plugin.suspendtrans.SuspendTransformConfiguration() {
    @Deprecated("Please use the " +
            "`love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration` " +
            "(`suspendTransformPlugin { ... }`) instead.")
    override var enabled: Boolean = true

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
