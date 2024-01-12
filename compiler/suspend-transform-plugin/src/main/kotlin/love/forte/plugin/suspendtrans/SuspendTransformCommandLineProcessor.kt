package love.forte.plugin.suspendtrans

import BuildConfig
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey


@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class SuspendTransformCommandLineProcessor : CommandLineProcessor {
    companion object {
        val CONFIGURATION: CompilerConfigurationKey<SuspendTransformConfiguration> =
            CompilerConfigurationKey.create(CliOptions.CONFIGURATION)
    }

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override val pluginOptions: Collection<SimpleCliOption> = CliOptions.allOptions.map { it as SimpleCliOption }

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        fun getConf(): SuspendTransformConfiguration {
            return configuration[CONFIGURATION] ?: SuspendTransformConfiguration().also {
                configuration.put(
                    CONFIGURATION,
                    it
                )
            }
        }

        CliOptions.allOptionsMap[option.optionName]?.resolveFromValue(getConf(), value)
    }
}
