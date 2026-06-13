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

import love.forte.plugin.suspendtrans.configuration.IncludeAnnotation
import love.forte.plugin.suspendtrans.utils.createIrBuilder
import love.forte.plugin.suspendtrans.utils.irAnnotationConstructor
import love.forte.plugin.suspendtrans.utils.toClassId
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.parentAsClass

/**
 * Adds configured annotations back to the generated origin function once its body exists.
 */
internal fun SuspendTransformTransformer.postProcessGenerateOriginFunction(
    function: IrFunction,
    originFunctionIncludeAnnotations: List<IncludeAnnotation>
) {
    function.annotations = buildList {
        val currentAnnotations = function.annotations
        fun hasAnnotation(name: org.jetbrains.kotlin.name.FqName): Boolean =
            currentAnnotations.any { a -> a.hasEqualAnnotationFqName(name) }
        addAll(currentAnnotations)

        originFunctionIncludeAnnotations.forEach { include ->
            val classId = include.classInfo.toClassId()
            val annotationClass = pluginContext.referenceClass(classId) ?: return@forEach
            if (!include.repeatable && hasAnnotation(classId.asSingleFqName())) {
                return@forEach
            }

            add(pluginContext.createIrBuilder(function.symbol).irAnnotationConstructor(annotationClass))
        }
    }
}

/**
 * Checks whether the annotation call already represents the given annotation fqName.
 */
private fun IrConstructorCall.hasEqualAnnotationFqName(name: org.jetbrains.kotlin.name.FqName): Boolean {
    return symbol.owner.parentAsClass.classId?.asSingleFqName() == name
}
