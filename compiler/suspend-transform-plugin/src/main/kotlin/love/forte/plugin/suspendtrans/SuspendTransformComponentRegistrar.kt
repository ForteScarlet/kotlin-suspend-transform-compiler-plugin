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

import love.forte.plugin.suspendtrans.configuration.InternalSuspendTransformConfigurationApi
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.fir.SuspendTransformFirExtensionRegistrar
import love.forte.plugin.suspendtrans.ir.SuspendTransformIrGenerationExtension
import love.forte.plugin.suspendtrans.symbol.SuspendTransformSyntheticResolveExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@OptIn(ExperimentalCompilerApi::class)
class SuspendTransformComponentRegistrar : CompilerPluginRegistrar() {

    //internal var defaultConfiguration: SuspendTransformConfiguration? = null

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        register(this, configuration)
    }


    companion object {
        fun register(storage: ExtensionStorage, configuration: CompilerConfiguration) {
            val suspendTransformConfiguration =/* defaultConfiguration ?: */
                configuration.resolveToSuspendTransformConfiguration()

            register(storage, suspendTransformConfiguration)
        }

        fun register(storage: ExtensionStorage, configuration: SuspendTransformConfiguration) {
            val suspendTransformSyntheticResolveExtension =
                SuspendTransformSyntheticResolveExtension(configuration)
            val suspendTransformFirExtensionRegistrar =
                SuspendTransformFirExtensionRegistrar(configuration)

            val suspendTransformIrGenerationExtension =
                SuspendTransformIrGenerationExtension(configuration)

            with(storage) {
                SyntheticResolveExtension.registerExtension(suspendTransformSyntheticResolveExtension)
                FirExtensionRegistrarAdapter.registerExtension(suspendTransformFirExtensionRegistrar)
                IrGenerationExtension.registerExtension(suspendTransformIrGenerationExtension)
            }
        }
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
