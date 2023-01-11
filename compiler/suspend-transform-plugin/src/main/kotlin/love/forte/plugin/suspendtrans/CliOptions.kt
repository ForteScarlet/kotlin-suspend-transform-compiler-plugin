package love.forte.plugin.suspendtrans

import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import kotlin.reflect.KMutableProperty

private val defaultJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

object CliOptions {

    const val CONFIGURATION = "configuration"

    private val RAW_CONFIGURATION = option(
        name = "raw_configuration",
        valueDescription = "Serialize the results in JSON format for configuration information",
        description = "Serialize the results in JSON format for configuration information",
    ) {
        inc { defaultJson.decodeFromString(SuspendTransformConfiguration.serializer(), it) }
        out { defaultJson.encodeToString(SuspendTransformConfiguration.serializer(), this) }
    }

    private val ENABLED = option("enabled") {
        inc { enabled = it.toBoolean() }
        out { enabled.toString() }
    }


    val allOptions: List<ICliOption> = listOf(
        ENABLED,
        RAW_CONFIGURATION
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

    fun withNullableProp(block: SuspendTransformConfiguration.() -> KMutableProperty<String?>) {
        inc { block().setter.call(it.takeIf { it.isNotEmpty() }) }
        out { block().getter.call() ?: "" }
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
