package love.forte.plugin.suspendtrans

import BuildConfig
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey


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
//
//        when (option.optionName) {
//            CliOptions.ENABLED.optionName -> inConf { enabled = value.toBoolean() }
//            CliOptions.Jvm.JVM_BLOCKING_FUNCTION_NAME.optionName -> inConf {
//                jvm { jvmBlockingFunctionName = value }
//            }
//
//            CliOptions.Jvm.JVM_ASYNC_FUNCTION_NAME.optionName -> inConf {
//                jvm { jvmAsyncFunctionName = value }
//            }
//
//            CliOptions.Jvm.JVM_BLOCKING_MARK_ANNOTATION_ANNOTATION_NAME.optionName -> inConf {
//                jvm { jvmBlockingMarkAnnotation.annotationName = value }
//            }
//
//            CliOptions.Jvm.JVM_BLOCKING_MARK_ANNOTATION_BASE_NAME_PROPERTY.optionName -> inConf {
//                jvm { jvmBlockingMarkAnnotation.baseNameProperty = value }
//            }
//
//            CliOptions.Jvm.JVM_BLOCKING_MARK_ANNOTATION_SUFFIX_PROPERTY.optionName -> inConf {
//                jvm { jvmBlockingMarkAnnotation.suffixProperty = value }
//            }
//
//            CliOptions.Jvm.JVM_BLOCKING_MARK_ANNOTATION_AS_PROPERTY_PROPERTY.optionName -> inConf {
//                jvm { jvmBlockingMarkAnnotation.asPropertyProperty = value }
//            }
//
//
//            else -> System.err.println("Unexpected config option ${option.optionName}")
//        }
    }
}
