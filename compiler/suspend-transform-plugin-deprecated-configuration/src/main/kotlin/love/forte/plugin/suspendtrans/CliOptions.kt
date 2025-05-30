@file:Suppress("DEPRECATION")

package love.forte.plugin.suspendtrans

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption

private val defaultJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Deprecated("Use the cli module's type")
object CliOptions {

    const val CONFIGURATION = "configuration"

    /** 冒号转义符 */
    private const val COLON_ESCAPE_CHARACTER = "&%1_"
    /** 逗号转义符 */
    private const val COMMA_ESCAPE_CHARACTER = "&%2_"

    private val rawRegex = Regex("[:,]")
    private val encodeRegex = Regex("(&%1_|&%2_)")


    private val TRANSFORMERS = option(
        name = "transformers",
        valueDescription = "Serialize the results in JSON format for configuration information",
        description = "Serialize the results in JSON format for configuration information",
    ) {
        // MutableMap<TargetPlatform, MutableList<Transformer>>
        val serializer = MapSerializer(TargetPlatform.serializer(), ListSerializer(Transformer.serializer()))

        inc {
            val jsonStr = encodeRegex.replace(it) { result ->
                when (val value = result.value) {
                    COLON_ESCAPE_CHARACTER -> ":"
                    COMMA_ESCAPE_CHARACTER -> ","
                    else -> value
                }
            }

            transformers = defaultJson.decodeFromString(serializer, jsonStr).toMutableMap()
        }
        out {
            val encoded = defaultJson.encodeToString(serializer, this.transformers)

            rawRegex.replace(encoded) { result ->
                when (val value = result.value) {
                    ":" -> COLON_ESCAPE_CHARACTER
                    "," -> COMMA_ESCAPE_CHARACTER
                    else -> value
                }
            }

        }
    }

    private val ENABLED = option("enabled") {
        inc { enabled = it.toBoolean() }
        out { enabled.toString() }
    }


    val allOptions: List<ICliOption> = listOf(
        ENABLED,
        TRANSFORMERS
    )
    val allOptionsMap = allOptions.associateBy { it.oName }

}

@Deprecated("Use the cli module's type")
private class ResolveBuilder {
    var outc: SuspendTransformConfiguration.() -> String = { error("no outc") }
    var inc: SuspendTransformConfiguration.(String) -> Unit = { error("no inc") }

    fun inc(block: SuspendTransformConfiguration.(String) -> Unit) {
        inc = block
    }

    fun out(block: SuspendTransformConfiguration.() -> String) {
        outc = block
    }

//    fun withProp(block: SuspendTransformConfiguration.() -> KMutableProperty<String>) {
//        inc { block().setter.call(it) }
//        out { block().getter.call() }
//    }
//
//    fun withNullableProp(block: SuspendTransformConfiguration.() -> KMutableProperty<String?>) {
//        inc { block().setter.call(it.takeIf { it.isNotEmpty() }) }
//        out { block().getter.call() ?: "" }
//    }
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

@Deprecated("Use the cli module's type")
interface ICliOption {
    val oName: String
    fun resolveToValue(configuration: SuspendTransformConfiguration): String
    fun resolveFromValue(configuration: SuspendTransformConfiguration, value: String)
}

@Deprecated("Use the cli module's type")
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
