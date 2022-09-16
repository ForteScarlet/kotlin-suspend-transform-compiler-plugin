package love.forte.plugin.suspendtrans

import BuildConfig
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey


@AutoService(CommandLineProcessor::class)
class SuspendTransformCommandLineProcessor : CommandLineProcessor {
    companion object {
        private object Options {
            const val ENABLED = "enabled"
        }

        val ENABLED: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey<Boolean>(Options.ENABLED)


    }

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = Options.ENABLED,
            valueDescription = "bool <true | false>",
            description = "If the DebugLog annotation should be applied",
            required = false,
        ),
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        return when (option.optionName) {
            Options.ENABLED -> configuration.put(ENABLED, value.toBoolean())
            else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
        }
    }
}
