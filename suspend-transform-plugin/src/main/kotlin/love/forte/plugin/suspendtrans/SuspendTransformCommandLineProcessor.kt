package love.forte.plugin.suspendtrans

import com.google.auto.service.AutoService
import love.forte.plugin.BuildConfig
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey


@AutoService(CommandLineProcessor::class)
class SuspendTransformCommandLineProcessor : CommandLineProcessor {
    companion object {
        private const val OPTION_ENABLED = "enabled"
        // private const val OPTION_JVM_BLOCKING_ANNOTATION: String = "jvmBlockingIncludeAnnotations"
        // private const val OPTION_JVM_ASYNC_ANNOTATION: String = "jvmAsyncIncludeAnnotations"
        // private const val OPTION_JS_PROMISE_ANNOTATION: String = "jsPromiseIncludeAnnotations"
        
        val ARG_ENABLED: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey<Boolean>(OPTION_ENABLED)
    }
    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID.also {
        println("Plugin ID: $it")
    }
    
    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = OPTION_ENABLED,
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
            OPTION_ENABLED -> configuration.put(ARG_ENABLED, value.toBoolean())
            else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
        }
    }
}
