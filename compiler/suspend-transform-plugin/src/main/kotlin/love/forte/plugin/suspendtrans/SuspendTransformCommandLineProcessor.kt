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
import love.forte.plugin.suspendtrans.cli.SuspendTransformCliOptions
import love.forte.plugin.suspendtrans.cli.decodeSuspendTransformConfigurationFromHex
import love.forte.plugin.suspendtrans.cli.toAbstractCliOption
import love.forte.plugin.suspendtrans.configuration.InternalSuspendTransformConfigurationApi
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.configuration.plus
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
class SuspendTransformCommandLineProcessor : CommandLineProcessor {
    companion object {
        val CONFIGURATION_KEY: CompilerConfigurationKey<SuspendTransformConfiguration> =
            CompilerConfigurationKey.create(SuspendTransformCliOptions.CONFIGURATION)
    }

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(SuspendTransformCliOptions.CLI_CONFIGURATION.toAbstractCliOption())

    @OptIn(InternalSuspendTransformConfigurationApi::class)
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        // The 'configuration' option
        if (SuspendTransformCliOptions.CLI_CONFIGURATION.optionName == option.optionName) {
            // Decode from protobuf hex value
            val decodedConfiguration = decodeSuspendTransformConfigurationFromHex(value)
            val currentConfig = configuration[CONFIGURATION_KEY]
            if (currentConfig == null) {
                configuration.put(CONFIGURATION_KEY, decodedConfiguration)
            } else {
                configuration.put(CONFIGURATION_KEY, currentConfig + decodedConfiguration)
            }
        }
    }
}
