package love.forte.plugin.suspendtrans.cli

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption

/**
 *
 *
 * @author ForteScarlet
 */
interface SuspendTransformCliOption {
    val optionName: String
    val valueDescription: String
    val description: String
    val required: Boolean
    val allowMultipleOccurrences: Boolean
}

private data class SimpleSuspendTransformCliOption(
    override val allowMultipleOccurrences: Boolean,
    override val description: String,
    override val optionName: String,
    override val required: Boolean,
    override val valueDescription: String
) : SuspendTransformCliOption

private data class AbstractCliOptionImpl(
    override val allowMultipleOccurrences: Boolean,
    override val description: String,
    override val optionName: String,
    override val required: Boolean,
    override val valueDescription: String
) : AbstractCliOption, SuspendTransformCliOption

/**
 * Creates an instance of [SuspendTransformCliOption] to describe and define a CLI option.
 *
 * @param optionName The name of the option used to identify it in the CLI.
 * @param valueDescription love.forte.plugin.suspendtrans.A description of the option's value, defaults to the option name.
 * @param description love.forte.plugin.suspendtrans.A textual description of the option, defaults to an empty string.
 * @param required Whether this option is mandatory, defaults to not required (`false`).
 * @param allowMultipleOccurrences Whether this option can appear multiple times in the CLI,
 * defaults to not allowed (`false`).
 * @return Returns an instance of [SuspendTransformCliOption] implemented by [SimpleSuspendTransformCliOption].
 */
fun SuspendTransformCliOption(
    optionName: String,
    valueDescription: String = optionName,
    description: String = "",
    required: Boolean = false,
    allowMultipleOccurrences: Boolean = false
): SuspendTransformCliOption {
    // Create and return an instance of the concrete implementation class
    return SimpleSuspendTransformCliOption(
        allowMultipleOccurrences = allowMultipleOccurrences,
        description = description,
        optionName = optionName,
        required = required,
        valueDescription = valueDescription
    )
}

/**
 * Converts the current [SuspendTransformCliOption] instance to an [AbstractCliOption].
 * If the current object is already an [AbstractCliOption], it is returned directly;
 * otherwise, a new instance is created and returned.
 *
 * @return The converted [AbstractCliOption] instance
 */
fun SuspendTransformCliOption.toAbstractCliOption(): AbstractCliOption {
    return this as? AbstractCliOption ?: AbstractCliOptionImpl(
        allowMultipleOccurrences = allowMultipleOccurrences,
        description = description,
        optionName = optionName,
        required = required,
        valueDescription = valueDescription
    )
}



