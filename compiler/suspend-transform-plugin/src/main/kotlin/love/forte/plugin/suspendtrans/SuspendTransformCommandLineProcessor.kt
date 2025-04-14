package love.forte.plugin.suspendtrans

import BuildConfig
import love.forte.plugin.suspendtrans.cli.SuspendTransformCliOptions
import love.forte.plugin.suspendtrans.cli.decodeSuspendTransformConfigurationFromHex
import love.forte.plugin.suspendtrans.cli.toAbstractCliOption
import love.forte.plugin.suspendtrans.configuration.InternalSuspendTransformConstructorApi
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

    @OptIn(InternalSuspendTransformConstructorApi::class)
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
