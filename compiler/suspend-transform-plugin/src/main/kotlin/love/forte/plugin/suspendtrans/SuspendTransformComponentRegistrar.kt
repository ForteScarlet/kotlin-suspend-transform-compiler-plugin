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

package love.forte.plugin.suspendtrans

import BuildConfig
import love.forte.plugin.suspendtrans.configuration.InternalSuspendTransformConfigurationApi
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.fir.SuspendTransformFirExtensionRegistrar
import love.forte.plugin.suspendtrans.ir.SuspendTransformIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.report
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtSourcelessDiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

/**
 * Registers every compiler extension entry point used by the suspend-transform plugin.
 */
@OptIn(ExperimentalCompilerApi::class)
class SuspendTransformComponentRegistrar : CompilerPluginRegistrar() {

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    //internal var defaultConfiguration: SuspendTransformConfiguration? = null

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!configuration.isK2Compilation()) {
            configuration.reportK1IsUnsupported()
            return
        }

        register(this, configuration)
    }


    companion object {
        /**
         * Resolves plugin configuration from compiler arguments and registers all extensions.
         */
        fun register(storage: ExtensionStorage, configuration: CompilerConfiguration) {
            val suspendTransformConfiguration =/* defaultConfiguration ?: */
                configuration.resolveToSuspendTransformConfiguration()

            register(storage, suspendTransformConfiguration)
        }

        /**
         * 为给定配置注册 K2/FIR 前端与 IR 后端扩展。
         *
         * 合成声明只能由 FIR 生成，避免重新引入依赖 Descriptor 的 K1 解析路径。
         */
        fun register(storage: ExtensionStorage, configuration: SuspendTransformConfiguration) {
            val suspendTransformFirExtensionRegistrar =
                SuspendTransformFirExtensionRegistrar(configuration)

            val suspendTransformIrGenerationExtension =
                SuspendTransformIrGenerationExtension(configuration)

            with(storage) {
                FirExtensionRegistrarAdapter.registerExtension(suspendTransformFirExtensionRegistrar)
                IrGenerationExtension.registerExtension(suspendTransformIrGenerationExtension)
            }
        }
    }
}

/**
 * 判断当前编译是否使用 K2/FIR 前端。
 *
 * 插件已经移除 K1 的 Descriptor 声明生成路径，因此不能在 K1 下继续注册仅有一半的扩展，
 * 否则用户会得到“插件已加载但没有生成声明”的静默错误。
 */
private fun CompilerConfiguration.isK2Compilation(): Boolean =
    get(CommonConfigurationKeys.USE_FIR, false)

/**
 * 向编译器消息通道报告明确的 K1 不兼容错误。
 */
private fun CompilerConfiguration.reportK1IsUnsupported() {
    report(
        SuspendTransformCompilerDiagnostics.K1_NOT_SUPPORTED,
        "Suspend Transform compiler plugin requires the Kotlin K2 compiler. K1 is no longer supported.",
    )
}

private object SuspendTransformCompilerDiagnostics {
    val K1_NOT_SUPPORTED = KtSourcelessDiagnosticFactory(
        "SUSPEND_TRANSFORM_K1_NOT_SUPPORTED",
        Severity.ERROR,
        SuspendTransformCompilerDiagnosticMessages,
    )
}

private object SuspendTransformCompilerDiagnosticMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("SuspendTransformCompilerDiagnostics") { map ->
        map.put(SuspendTransformCompilerDiagnostics.K1_NOT_SUPPORTED, "{0}")
    }
}

// @Deprecated("Use Cli module's type")
// private fun CompilerConfiguration.resolveToSuspendTransformConfiguration(): SuspendTransformConfiguration {
// //    val compilerConfiguration = this
//     return get(SuspendTransformCommandLineProcessor.CONFIGURATION_KEY, SuspendTransformConfiguration())
// //    return SuspendTransformConfiguration().apply {
// //        enabled = compilerConfiguration.get(SuspendTransformCommandLineProcessor.ENABLED, true)
// //    }
// }

/**
 * Reads the merged suspend-transform configuration from compiler configuration storage.
 */
@OptIn(InternalSuspendTransformConfigurationApi::class)
private fun CompilerConfiguration.resolveToSuspendTransformConfiguration(): SuspendTransformConfiguration {
    return get(
        SuspendTransformCommandLineProcessor.CONFIGURATION_KEY,
        SuspendTransformConfiguration(mutableMapOf())
    )
//    return SuspendTransformConfiguration().apply {
//        enabled = compilerConfiguration.get(SuspendTransformCommandLineProcessor.ENABLED, true)
//    }
}
