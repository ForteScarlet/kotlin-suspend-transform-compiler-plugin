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

import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

/**
 * Locates the original suspend declaration for a generated member and wires a bridge body to it.
 *
 * @return the origin function when one was found and used.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
internal inline fun SuspendTransformTransformer.resolveFunctionBody(
    sourceKey: Any?,
    function: IrFunction,
    crossinline checkIsOriginFunction: (IrFunction) -> Boolean,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
): IrFunction? {
    if (function.body != null) {
        return null
    }

    val parent = function.parent
    if (parent !is IrDeclarationContainer) {
        return null
    }

    val originFunctions = parent.findOriginFunctions(checkIsOriginFunction)
    if (originFunctions.size != 1) {
        reportOriginFunctionSearchMismatch(function, parent, originFunctions, sourceKey)
        return null
    }

    val originFunction = originFunctions.first()
    reportGeneratedBodyResolution(function, originFunction)

    function.body = generateTransformBodyForFunctionLambda(
        pluginContext,
        function,
        originFunction,
        transformTargetFunctionCall,
    )

    return originFunction
}

/**
 * Searches the current container for the original suspend declaration that matches a
 * generated synthetic member.
 */
private inline fun IrDeclarationContainer.findOriginFunctions(
    crossinline checkIsOriginFunction: (IrFunction) -> Boolean,
): List<IrFunction> {
//        val originFunctionsSequence = sequence {
//            var p: IrDeclarationContainer? = parent
//            while (p != null) {
//                for (declaration in p.declarations) {
//                    if (declaration is IrFunction && checkIsOriginFunction(declaration)) {
//                        yield(declaration)
//                    }
//                }
//                val curr = p
//                p = if (curr is IrDeclaration) {
//                    (curr.parent as? IrDeclarationContainer).takeIf { it != curr }
//                } else {
//                    null
//                }
//            }
//        }

    /*
    Search the current container for the origin function.
    This generated function may come from an inherited declaration and therefore
    not carry the transform annotation in the current class.

    For example, `runBlocking` may be generated in interface `Foo`, while `FooImpl`
    is the declaration that gets visited here. In that case it is fine to skip the body.

    2024/03/24:
    A size == 0 situation also appears for subclasses without a manual implementation, e.g.
    ```
    interface Foo {
        @JvmBlocking
        fun run()
    }

    internal class FooImpl : Foo {
        // A warning may appear here even though the annotation is not on this override.
        override fun run() { ... }
    }
    ```
     */

    return declarations.asSequence()
        .filterIsInstance<IrFunction>()
        .filter { checkIsOriginFunction(it) }
        .take(2)
        .toList()
}

/**
 * Generates a bridge body directly when no origin lookup is required.
 */
internal fun SuspendTransformTransformer.resolveBridgeFunctionBody(
    sourceKey: Any?,
    function: IrFunction,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
) {
    if (function.body == null) {
        // body: return $transform(block, scope?)
        function.body = generateTransformBodyForFunctionLambda(
            pluginContext,
            function,
            null,
            transformTargetFunctionCall,
        )
    }
}
