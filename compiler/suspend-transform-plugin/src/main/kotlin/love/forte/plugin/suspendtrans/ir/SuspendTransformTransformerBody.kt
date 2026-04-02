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

import love.forte.plugin.suspendtrans.utils.createIrBuilder
import love.forte.plugin.suspendtrans.utils.createSuspendLambdaFunctionWithCoroutineScope
import love.forte.plugin.suspendtrans.utils.createSuspendLambdaWithCoroutineScope
import love.forte.plugin.suspendtrans.utils.paramsAndReceiversAsParamsList
import love.forte.plugin.suspendtrans.valueParameters0
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrBody
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.primaryConstructor

/**
 * Legacy body builder kept for parity with the pre-lambda implementation.
 */
@Deprecated("see generateTransformBodyForFunctionLambda")
internal fun generateTransformBodyForFunction(
    context: IrPluginContext,
    function: IrFunction,
    originFunction: IrFunction,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
): IrBody {
    // default params
    val originValueParameters = originFunction.valueParameters0()
    function.valueParameters0().forEachIndexed { index, parameter ->
        val originFunctionValueParameter = originValueParameters[index]
        parameter.defaultValue = originFunctionValueParameter.defaultValue
    }

    return context.createIrBuilder(function.symbol).irBlockBody {
        val suspendLambda = context.createSuspendLambdaWithCoroutineScope(
            parent = originFunction.parent,
            // suspend () -> ?
            lambdaType = context.irBuiltIns.suspendFunctionN(0).typeWith(originFunction.returnType),
            originFunction = originFunction
        ).also { +it }

        +irReturn(irCall(transformTargetFunctionCall).apply {
            arguments[0] = irCall(suspendLambda.primaryConstructor!!).apply {
                for ((index, parameter) in function.paramsAndReceiversAsParamsList().withIndex()) {
                    arguments[index] = irGet(parameter)
                }
            }
            // argument: 1, if is CoroutineScope, and this is CoroutineScope.
            //println("transformTargetFunctionCall.owner: ${transformTargetFunctionCall.owner}")
            //println(transformTargetFunctionCall.owner.valueParameters)
            val owner = transformTargetFunctionCall.owner

            // CoroutineScope
            val ownerValueParameters = owner.valueParameters0()

            if (ownerValueParameters.size > 1) {
                for (index in 1..ownerValueParameters.lastIndex) {
                    val valueParameter = ownerValueParameters[index]
                    val type = valueParameter.type
                    tryResolveCoroutineScopeValueParameter(type, context, function, owner, this@irBlockBody, index)
                }
            }

        })
    }
}

/**
 * Generates the transformed function body in two modes:
 * - `originFunction != null`: create a suspend lambda delegating to the recovered origin function.
 * - `originFunction == null`: bridge mode; use the incoming `block` parameter directly.
 */
internal fun generateTransformBodyForFunctionLambda(
    context: IrPluginContext,
    function: IrFunction,
    originFunction: IrFunction?,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
): IrBody {
    // When non-null, build from the origin; otherwise use bridge mode.
    originFunction?.valueParameters0()?.also { originValueParameters ->
        function.valueParameters0().forEachIndexed { index, parameter ->
            val originFunctionValueParameter = originValueParameters[index]
            parameter.defaultValue = originFunctionValueParameter.defaultValue
        }
    }

    return context.createIrBuilder(function.symbol).irBlockBody {
        val lambdaExpression: IrExpression = if (originFunction != null) {
            val suspendLambdaFunc = context.createSuspendLambdaFunctionWithCoroutineScope(
                originFunction = originFunction,
                function = function
            )

            val lambdaType = context.irBuiltIns.suspendFunctionN(0).typeWith(suspendLambdaFunc.returnType)

            IrFunctionExpressionImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                lambdaType,
                suspendLambdaFunc,
                IrStatementOrigin.LAMBDA
            )
        } else {
            // is bridge fun, use the first param `block`
            val blockParameter = function.valueParameters0().first()
            irGet(blockParameter)
        }

        +irReturn(irCall(transformTargetFunctionCall).apply {
            arguments[0] = lambdaExpression

            val transformFunctionOwner = transformTargetFunctionCall.owner

            // CoroutineScope
            val ownerValueParameters = transformFunctionOwner.valueParameters0()

            // argument: 1, if is CoroutineScope, and this is CoroutineScope.
            if (ownerValueParameters.size > 1) {
                for (index in 1..ownerValueParameters.lastIndex) {
                    val valueParameter = ownerValueParameters[index]
                    val type = valueParameter.type
                    tryResolveCoroutineScopeValueParameter(
                        type,
                        context,
                        function,
                        transformFunctionOwner,
                        this@irBlockBody,
                        index
                    )
                }
            }

        })
    }
}
