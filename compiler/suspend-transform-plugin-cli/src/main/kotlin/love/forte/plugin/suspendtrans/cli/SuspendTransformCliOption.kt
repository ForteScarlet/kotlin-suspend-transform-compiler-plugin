/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * @param valueDescription A description of the option's value, defaults to the option name.
 * @param description A textual description of the option, defaults to an empty string.
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



