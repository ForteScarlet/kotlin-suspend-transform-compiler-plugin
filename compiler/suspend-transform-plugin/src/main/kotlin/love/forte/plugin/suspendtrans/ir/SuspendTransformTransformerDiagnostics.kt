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

package love.forte.plugin.suspendtrans.ir

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.getSourceFile
import org.jetbrains.kotlin.ir.util.kotlinFqName

/**
 * Computes a source location that can be attached to diagnostics for generated IR bodies.
 */
internal fun IrFunction.reportLocation(): CompilerMessageSourceLocation? = runCatching {
    val file = getSourceFile() ?: return null
    val (line, column) = file.getLineAndColumnNumbers(startOffset)
    return CompilerMessageLocation.create(file.name, line, column, null)
}.getOrNull()

/**
 * Reports that resolving the origin function for a generated member produced an
 * unexpected number of matches in the current container.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
internal fun SuspendTransformTransformer.reportOriginFunctionSearchMismatch(
    function: IrFunction,
    parent: IrDeclarationContainer,
    originFunctions: List<IrFunction>,
    sourceKey: Any?,
) {
    val actualNum = if (originFunctions.isEmpty()) "0" else "more than ${originFunctions.size}"
    val message =
        "Synthetic function ${function.name.asString()}" +
            "(${kotlin.runCatching { function.kotlinFqName.asString() }.getOrElse { function.toString() }} " +
            "in " +
            "${kotlin.runCatching { (parent as IrDeclaration).kotlinFqName.asString() }.getOrElse { parent.toString() }}) 's " +
            "originFunctions.size should be 1, " +
            "but $actualNum (findIn = ${(parent as? IrDeclaration)?.descriptor}, originFunctions = $originFunctions, sourceKey = $sourceKey)"

    reportInfo(message, function.reportLocation())
}

/**
 * Reports that a generated declaration body is being synthesized from a matched origin function.
 */
internal fun SuspendTransformTransformer.reportGeneratedBodyResolution(
    function: IrFunction,
    originFunction: IrFunction,
) {
    reportInfo(
        "Generate body for function " +
            kotlin.runCatching { function.kotlinFqName.asString() }.getOrElse { function.name.asString() } +
            " by origin function " +
            kotlin.runCatching { originFunction.kotlinFqName.asString() }.getOrElse { originFunction.name.asString() },
        originFunction.reportLocation() ?: function.reportLocation(),
    )
}

/**
 * Emits informational diagnostics through the compiler reporter when available and
 * falls back to stderr otherwise.
 */
private fun SuspendTransformTransformer.reportInfo(
    message: String,
    location: CompilerMessageSourceLocation?,
) {
    if (reporter != null) {
        // WARN? DEBUG? IGNORE?
        reporter.report(
            CompilerMessageSeverity.INFO,
            message,
            location,
        )
    } else {
        // TODO In K2?
        System.err.println(message)
    }
}
