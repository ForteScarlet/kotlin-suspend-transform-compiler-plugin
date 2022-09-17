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
        val defaultJvm = SuspendTransformConfiguration.Jvm()
        val defaultJs = SuspendTransformConfiguration.Js()
        private object Options {
            const val CONFIGURATION = "configuration"
            const val ENABLED = "enabled"

            object Jvm {
                //region blocking
                const val JVM_BLOCKING_FUNCTION_NAME = "jvm.jvmBlockingFunctionName"

                //region blocking mark
                const val JVM_BLOCKING_MARK_ANNOTATION = "jvm.jvmBlockingMarkAnnotation"
                const val JVM_BLOCKING_MARK_ANNOTATION_ANNOTATION_NAME = "jvm.jvmBlockingMarkAnnotation.annotationName"
                const val JVM_BLOCKING_MARK_ANNOTATION_BASE_NAME_PROPERTY = "jvm.jvmBlockingMarkAnnotation.baseNameProperty"
                const val JVM_BLOCKING_MARK_ANNOTATION_SUFFIX_PROPERTY = "jvm.jvmBlockingMarkAnnotation.suffixProperty"
                const val JVM_BLOCKING_MARK_ANNOTATION_AS_PROPERTY_PROPERTY = "jvm.jvmBlockingMarkAnnotation.asPropertyProperty"
                //endregion
                //endregion



                //region async mark
                const val JVM_ASYNC_MARK_ANNOTATION = "jvm.jvmAsyncMarkAnnotation"
                const val JVM_ASYNC_MARK_ANNOTATION_ANNOTATION_NAME = "jvm.jvmAsyncMarkAnnotation.annotationName"
                const val JVM_ASYNC_MARK_ANNOTATION_BASE_NAME_PROPERTY = "jvm.jvmAsyncMarkAnnotation.baseNameProperty"
                const val JVM_ASYNC_MARK_ANNOTATION_SUFFIX_PROPERTY = "jvm.jvmAsyncMarkAnnotation.suffixProperty"
                const val JVM_ASYNC_MARK_ANNOTATION_AS_PROPERTY_PROPERTY = "jvm.jvmAsyncMarkAnnotation.asPropertyProperty"
                //endregion



            }

            object Js
        }

        val CONFIGURATION: CompilerConfigurationKey<SuspendTransformConfiguration> = CompilerConfigurationKey.create(Options.CONFIGURATION)
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
        fun getConf(): SuspendTransformConfiguration {
            return configuration[CONFIGURATION] ?: SuspendTransformConfiguration().also { configuration.put(CONFIGURATION, it) }
        }
        fun inConf(block: SuspendTransformConfiguration.() -> Unit) {
            getConf().block()
        }
        when (option.optionName) {
            Options.ENABLED -> inConf { enabled = value.toBoolean() }
            Options.Jvm.JVM_BLOCKING_FUNCTION_NAME -> inConf {
                jvm { jvmBlockingFunctionName = value }
            }
//            //region jvm blocking mark annotation
//            Options.Jvm.JVM_BLOCKING_MARK_ANNOTATION_ANNOTATION_NAME -> {
//                getOrInit(JVM_BLOCKING_MARK_ANNOTATION) { defaultJvm.jvmBlockingMarkAnnotation }.also {
//                    it.annotationName = value
//                }
//            }
//            Options.Jvm.JVM_BLOCKING_MARK_ANNOTATION_BASE_NAME_PROPERTY -> {
//                getOrInit(JVM_BLOCKING_MARK_ANNOTATION) { defaultJvm.jvmBlockingMarkAnnotation }.also {
//                    it.baseNameProperty = value
//                }
//            }
//            Options.Jvm.JVM_BLOCKING_MARK_ANNOTATION_SUFFIX_PROPERTY -> {
//                getOrInit(JVM_BLOCKING_MARK_ANNOTATION) { defaultJvm.jvmBlockingMarkAnnotation }.also {
//                    it.suffixProperty = value
//                }
//            }
//            Options.Jvm.JVM_BLOCKING_MARK_ANNOTATION_AS_PROPERTY_PROPERTY -> {
//                getOrInit(JVM_BLOCKING_MARK_ANNOTATION) { defaultJvm.jvmBlockingMarkAnnotation }.also {
//                    it.asPropertyProperty = value
//                }
//            }
//            //endregion
//            //region jvm async mark annotation
//            Options.Jvm.JVM_ASYNC_MARK_ANNOTATION_ANNOTATION_NAME -> {
//                getOrInit(JVM_ASYNC_MARK_ANNOTATION) { defaultJvm.jvmAsyncMarkAnnotation }.also {
//                    it.annotationName = value
//                }
//            }
//            Options.Jvm.JVM_ASYNC_MARK_ANNOTATION_BASE_NAME_PROPERTY -> {
//                getOrInit(JVM_ASYNC_MARK_ANNOTATION) { defaultJvm.jvmAsyncMarkAnnotation }.also {
//                    it.baseNameProperty = value
//                }
//            }
//            Options.Jvm.JVM_ASYNC_MARK_ANNOTATION_SUFFIX_PROPERTY -> {
//                getOrInit(JVM_ASYNC_MARK_ANNOTATION) { defaultJvm.jvmAsyncMarkAnnotation }.also {
//                    it.suffixProperty = value
//                }
//            }
//            Options.Jvm.JVM_ASYNC_MARK_ANNOTATION_AS_PROPERTY_PROPERTY -> {
//                getOrInit(JVM_ASYNC_MARK_ANNOTATION) { defaultJvm.jvmAsyncMarkAnnotation }.also {
//                    it.asPropertyProperty = value
//                }
//            }
//            //endregion



            else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
        }
    }
}
