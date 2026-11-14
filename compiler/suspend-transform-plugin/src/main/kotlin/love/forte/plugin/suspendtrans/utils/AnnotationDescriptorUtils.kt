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

package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irAnnotation
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.name.Name


fun IrBuilderWithScope.irAnnotationConstructor(
    clazz: IrClassSymbol,
): IrConstructorCall {
    return irCall(clazz.constructors.first()).let {
        irConstructorCall(it, it.symbol)
    }
}

fun IrBuilderWithScope.irAnnotation(symbol: IrConstructorSymbol): IrAnnotation {
    return irAnnotation(symbol)
}

data class TransformAnnotationData(
    val baseName: String?,
    val suffix: String?,
    val rawAsProperty: Boolean?,
    val asProperty: Boolean,
    val functionName: String,
    val markName: String?,
) {
    companion object {
        fun of(
            session: FirSession,
            firAnnotation: FirAnnotation,
            annotationBaseNamePropertyName: String = "baseName",
            annotationSuffixPropertyName: String = "suffix",
            annotationAsPropertyPropertyName: String = "asProperty",
            annotationMarkNamePropertyName: String? = null,
            defaultBaseName: String,
            defaultSuffix: String,
            defaultAsProperty: Boolean,
        ): TransformAnnotationData {
            val baseName = firAnnotation.getStringArgument0(Name.identifier(annotationBaseNamePropertyName))
                ?.takeIf { it.isNotEmpty() }

            val suffix = firAnnotation.getStringArgument0(Name.identifier(annotationSuffixPropertyName))

            val rawAsProperty =
                firAnnotation.getBooleanArgument0(Name.identifier(annotationAsPropertyPropertyName))

            val functionName = "${baseName ?: defaultBaseName}${suffix ?: defaultSuffix}"

            val markName = if (annotationMarkNamePropertyName != null) {
                firAnnotation.getStringArgument0(Name.identifier(annotationMarkNamePropertyName))
                    ?.takeIf { it.isNotEmpty() }
            } else {
                null
            }

            return TransformAnnotationData(
                baseName,
                suffix,
                rawAsProperty,
                rawAsProperty ?: defaultAsProperty,
                functionName,
                markName
            )
        }
    }
}

private fun FirAnnotation.getStringArgument0(name: Name): String? {
    val arg = getStringArgument(name)
    if (arg != null) {
        return arg
    }

    // If not found, try to use `findArgumentByName`
    val argByName = findArgumentByName(name, returnFirstWhenNotFound = false)
    return (argByName as? FirLiteralExpression)?.value as? String
}

private fun FirAnnotation.getBooleanArgument0(name: Name): Boolean? {
    val arg = getBooleanArgument(name)
    if (arg != null) {
        return arg
    }

    // If not found, try to use `findArgumentByName`
    val argByName = findArgumentByName(name, returnFirstWhenNotFound = false)
    return (argByName as? FirLiteralExpression)?.value as? Boolean
}

