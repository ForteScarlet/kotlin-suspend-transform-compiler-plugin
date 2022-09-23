package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import kotlin.reflect.KMutableProperty

private typealias ExcludeAnnotation = SuspendTransformConfiguration.ExcludeAnnotation

private const val REPEAT_MARK = "\$&+"

private fun includeAnnotationEncode(value: List<SuspendTransformConfiguration.IncludeAnnotation>): String {
    return value.joinToString(",") { if (it.repeatable) "${it.name}$REPEAT_MARK" else it.name }
}

private fun includeAnnotationDecode(value: String): List<SuspendTransformConfiguration.IncludeAnnotation> {
    return value.split(",").map {
        val annotationName = it.substringBeforeLast(REPEAT_MARK)
        val repeat = annotationName == it
        SuspendTransformConfiguration.IncludeAnnotation(annotationName, repeat)
    }
}

object CliOptions {

    const val CONFIGURATION = "configuration"
    val ENABLED = option("enabled") {
        inc { enabled = it.toBoolean() }
        out { enabled.toString() }
    }

    object Jvm {

        val ORIGIN_FUNCTION_INCLUDE_ANNOTATIONS = option("jvm.originFunctionIncludeAnnotations") {
            inc { jvm.originFunctionIncludeAnnotations = includeAnnotationDecode(it) }
            out { includeAnnotationEncode(jvm.originFunctionIncludeAnnotations) }
        }


        //region blocking
        val JVM_BLOCKING_FUNCTION_NAME = option("jvm.jvmBlockingFunctionName") {
            withProp { jvm::jvmBlockingFunctionName }
        }

        //region blocking mark
        const val JVM_BLOCKING_MARK_ANNOTATION = "jvm.jvmBlockingMarkAnnotation"

        val JVM_BLOCKING_MARK_ANNOTATION_ANNOTATION_NAME =
            JVM_BLOCKING_MARK_ANNOTATION.option("annotationName") {
                withProp { jvm.jvmBlockingMarkAnnotation::annotationName }
            }
        val JVM_BLOCKING_MARK_ANNOTATION_BASE_NAME_PROPERTY =
            JVM_BLOCKING_MARK_ANNOTATION.option("baseNameProperty") {
                withProp { jvm.jvmBlockingMarkAnnotation::baseNameProperty }
            }
        val JVM_BLOCKING_MARK_ANNOTATION_SUFFIX_PROPERTY =
            JVM_BLOCKING_MARK_ANNOTATION.option("suffixProperty") {
                withProp { jvm.jvmBlockingMarkAnnotation::suffixProperty }
            }
        val JVM_BLOCKING_MARK_ANNOTATION_AS_PROPERTY_PROPERTY =
            JVM_BLOCKING_MARK_ANNOTATION.option("asPropertyProperty") {
                withProp { jvm.jvmBlockingMarkAnnotation::asPropertyProperty }
            }
        val JVM_BLOCKING_MARK_ANNOTATION_FUNCTION_INHERITABLE =
            JVM_BLOCKING_MARK_ANNOTATION.option("functionInheritable") {
                inc { jvm.jvmBlockingMarkAnnotation.functionInheritable = it.toBoolean()  }
                out { jvm.jvmBlockingMarkAnnotation.functionInheritable.toString() }
            }
        //endregion

        val COPY_ANNOTATIONS_TO_SYNTHETIC_BLOCKING_FUNCTION =
            option("jvm.copyAnnotationsToSyntheticBlockingFunction") {
                inc { jvm.copyAnnotationsToSyntheticBlockingFunction = it.toBoolean() }
                out { jvm.copyAnnotationsToSyntheticBlockingFunction.toString() }
            }
        const val COPY_ANNOTATIONS_TO_SYNTHETIC_BLOCKING_FUNCTION_EXCLUDES =
            "jvm.copyAnnotationsToSyntheticBlockingFunctionExcludes"

        val COPY_ANNOTATIONS_TO_SYNTHETIC_BLOCKING_FUNCTION_EXCLUDES_NAME =
            COPY_ANNOTATIONS_TO_SYNTHETIC_BLOCKING_FUNCTION_EXCLUDES.option("name") {
                inc { jvm.copyAnnotationsToSyntheticBlockingFunctionExcludes = it.split(",").map(::ExcludeAnnotation) }
                out { jvm.copyAnnotationsToSyntheticBlockingFunctionExcludes.joinToString(",") { it.name } }

            }

        val SYNTHETIC_BLOCKING_FUNCTION_INCLUDE_ANNOTATIONS =
            option("jvm.syntheticBlockingFunctionIncludeAnnotations") {
                inc { jvm.syntheticBlockingFunctionIncludeAnnotations = includeAnnotationDecode(it) }
                out { includeAnnotationEncode(jvm.syntheticBlockingFunctionIncludeAnnotations) }
            }

        //endregion


        //region async
        val JVM_ASYNC_FUNCTION_NAME = option("jvm.jvmAsyncFunctionName") {
            withProp { jvm::jvmAsyncFunctionName }
        }

        //region async mark
        const val JVM_ASYNC_MARK_ANNOTATION = "jvm.jvmAsyncMarkAnnotation"

        val JVM_ASYNC_MARK_ANNOTATION_ANNOTATION_NAME =
            JVM_ASYNC_MARK_ANNOTATION.option("annotationName") {
                withProp { jvm.jvmAsyncMarkAnnotation::annotationName }
            }
        val JVM_ASYNC_MARK_ANNOTATION_BASE_NAME_PROPERTY =
            JVM_ASYNC_MARK_ANNOTATION.option("baseNameProperty") {
                withProp { jvm.jvmAsyncMarkAnnotation::baseNameProperty }
            }
        val JVM_ASYNC_MARK_ANNOTATION_SUFFIX_PROPERTY =
            JVM_ASYNC_MARK_ANNOTATION.option("suffixProperty") {
                withProp { jvm.jvmAsyncMarkAnnotation::suffixProperty }
            }
        val JVM_ASYNC_MARK_ANNOTATION_AS_PROPERTY_PROPERTY =
            JVM_ASYNC_MARK_ANNOTATION.option("asPropertyProperty") {
                withProp { jvm.jvmAsyncMarkAnnotation::asPropertyProperty }
            }
        val JVM_ASYNC_MARK_ANNOTATION_FUNCTION_INHERITABLE =
            JVM_ASYNC_MARK_ANNOTATION.option("functionInheritable") {
                inc { jvm.jvmAsyncMarkAnnotation.functionInheritable = it.toBoolean()  }
                out { jvm.jvmAsyncMarkAnnotation.functionInheritable.toString() }
            }
        //endregion


        val SYNTHETIC_ASYNC_FUNCTION_INCLUDE_ANNOTATIONS = option("jvm.syntheticAsyncFunctionIncludeAnnotations") {
            inc { jvm.syntheticAsyncFunctionIncludeAnnotations = includeAnnotationDecode(it) }
            out { includeAnnotationEncode(jvm.syntheticAsyncFunctionIncludeAnnotations) }
        }

        val COPY_ANNOTATIONS_TO_SYNTHETIC_ASYNC_FUNCTION =
            option("jvm.copyAnnotationsToSyntheticAsyncFunction") {
                inc { jvm.copyAnnotationsToSyntheticAsyncFunction = it.toBoolean() }
                out { jvm.copyAnnotationsToSyntheticAsyncFunction.toString() }
            }
        const val COPY_ANNOTATIONS_TO_SYNTHETIC_ASYNC_FUNCTION_EXCLUDES =
            "jvm.copyAnnotationsToSyntheticAsyncFunctionExcludes"

        val COPY_ANNOTATIONS_TO_SYNTHETIC_ASYNC_FUNCTION_EXCLUDES_NAME =
            COPY_ANNOTATIONS_TO_SYNTHETIC_ASYNC_FUNCTION_EXCLUDES.option("name") {
                inc { jvm.copyAnnotationsToSyntheticAsyncFunctionExcludes = it.split(",").map(::ExcludeAnnotation) }
                out { jvm.copyAnnotationsToSyntheticAsyncFunctionExcludes.joinToString(",") { it.name } }
            }

        //endregion


    }

    object Js


    val allOptions: List<ICliOption> = listOf(
        ENABLED,
        Jvm.ORIGIN_FUNCTION_INCLUDE_ANNOTATIONS,
        Jvm.JVM_BLOCKING_FUNCTION_NAME,
        Jvm.JVM_BLOCKING_MARK_ANNOTATION_ANNOTATION_NAME,
        Jvm.JVM_BLOCKING_MARK_ANNOTATION_BASE_NAME_PROPERTY,
        Jvm.JVM_BLOCKING_MARK_ANNOTATION_SUFFIX_PROPERTY,
        Jvm.JVM_BLOCKING_MARK_ANNOTATION_AS_PROPERTY_PROPERTY,
        Jvm.JVM_BLOCKING_MARK_ANNOTATION_FUNCTION_INHERITABLE,
        Jvm.COPY_ANNOTATIONS_TO_SYNTHETIC_BLOCKING_FUNCTION,
        Jvm.COPY_ANNOTATIONS_TO_SYNTHETIC_BLOCKING_FUNCTION_EXCLUDES_NAME,
        Jvm.SYNTHETIC_BLOCKING_FUNCTION_INCLUDE_ANNOTATIONS,
        Jvm.JVM_ASYNC_FUNCTION_NAME,
        Jvm.JVM_ASYNC_MARK_ANNOTATION_ANNOTATION_NAME,
        Jvm.JVM_ASYNC_MARK_ANNOTATION_BASE_NAME_PROPERTY,
        Jvm.JVM_ASYNC_MARK_ANNOTATION_SUFFIX_PROPERTY,
        Jvm.JVM_ASYNC_MARK_ANNOTATION_AS_PROPERTY_PROPERTY,
        Jvm.JVM_ASYNC_MARK_ANNOTATION_FUNCTION_INHERITABLE,
        Jvm.SYNTHETIC_ASYNC_FUNCTION_INCLUDE_ANNOTATIONS,
        Jvm.COPY_ANNOTATIONS_TO_SYNTHETIC_ASYNC_FUNCTION,
        Jvm.COPY_ANNOTATIONS_TO_SYNTHETIC_ASYNC_FUNCTION_EXCLUDES_NAME,
    )
    val allOptionsMap = allOptions.associateBy { it.oName }

}

private class ResolveBuilder {
    var outc: SuspendTransformConfiguration.() -> String = { error("no outc") }
    var inc: SuspendTransformConfiguration.(String) -> Unit = { error("no inc") }

    fun inc(block: SuspendTransformConfiguration.(String) -> Unit) {
        inc = block
    }

    fun out(block: SuspendTransformConfiguration.() -> String) {
        outc = block
    }

    fun withProp(block: SuspendTransformConfiguration.() -> KMutableProperty<String>) {
        inc { block().setter.call(it) }
        out { block().getter.call() }
    }
}

private fun option(
    name: String,
    valueDescription: String = name,
    description: String = name,
    required: Boolean = false,
    allowMultipleOccurrences: Boolean = false,
    block: ResolveBuilder.() -> Unit
): SimpleCliOption {
    val builder = ResolveBuilder().also(block)
    return SimpleCliOption(
        optionName = name,
        valueDescription = valueDescription,
        description = description,
        required = required,
        allowMultipleOccurrences = allowMultipleOccurrences,
        builder.outc,
        builder.inc
    )
}

private fun String.option(
    subName: String,
    valueDescription: String = subName,
    description: String = "",
    required: Boolean = false,
    allowMultipleOccurrences: Boolean = false,
    block: ResolveBuilder.() -> Unit
): SimpleCliOption {
    return option(
        name = "$this.$subName",
        valueDescription = valueDescription,
        description = description,
        required = required,
        allowMultipleOccurrences = allowMultipleOccurrences,
        block
    )
}

interface ICliOption {
    val oName: String
    fun resolveToValue(configuration: SuspendTransformConfiguration): String
    fun resolveFromValue(configuration: SuspendTransformConfiguration, value: String)
}

class SimpleCliOption(
    override val optionName: String,
    override val valueDescription: String,
    override val description: String,
    override val required: Boolean = true,
    override val allowMultipleOccurrences: Boolean = false,
    private val _resolveToValue: SuspendTransformConfiguration.() -> String,
    private val _resolveFromValue: SuspendTransformConfiguration.(String) -> Unit
) : ICliOption, AbstractCliOption {
    override val oName: String
        get() = optionName

    override fun resolveToValue(configuration: SuspendTransformConfiguration): String = _resolveToValue(configuration)
    override fun resolveFromValue(configuration: SuspendTransformConfiguration, value: String) =
        _resolveFromValue(configuration, value)
}