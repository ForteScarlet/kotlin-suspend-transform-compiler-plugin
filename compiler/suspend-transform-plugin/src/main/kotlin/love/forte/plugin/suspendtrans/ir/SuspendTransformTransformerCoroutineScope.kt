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

import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.utils.isClassType
import love.forte.plugin.suspendtrans.valueParameters0
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.isSubtypeOfClass
import org.jetbrains.kotlin.name.ClassId

private val coroutineScopeTypeName = "kotlinx.coroutines.CoroutineScope".fqn
private val coroutineScopeTypeClassId = ClassId.topLevel("kotlinx.coroutines.CoroutineScope".fqn)
private val coroutineScopeTypeNameUnsafe = coroutineScopeTypeName.toUnsafe()

/**
 * Resolves optional transform call arguments that expect `CoroutineScope`.
 *
 * When the current call parameter type is `CoroutineScope`:
 * - if dispatch receiver is already a `CoroutineScope`, pass `this`;
 * - otherwise, if transform parameter is nullable, pass `this as? CoroutineScope`;
 * - otherwise leave the argument untouched (typically relying on default values).
 */
internal fun IrCall.tryResolveCoroutineScopeValueParameter(
    type: IrType,
    context: IrPluginContext,
    function: IrFunction,
    owner: IrSimpleFunction,
    builderWithScope: IrBuilderWithScope,
    index: Int
) {
    if (!type.isClassType(coroutineScopeTypeNameUnsafe)) {
        return
    }

    function.dispatchReceiverParameter?.also { dispatchReceiverParameter ->
        context.referenceClass(coroutineScopeTypeClassId)?.also { coroutineScopeRef ->
            if (dispatchReceiverParameter.type.isSubtypeOfClass(coroutineScopeRef)) {
                // put 'this' to the arg
                arguments[index] = builderWithScope.irGet(dispatchReceiverParameter)
            } else {
                val scopeType = coroutineScopeRef.defaultType

                val scopeParameter = owner.valueParameters0().getOrNull(1)

                if (scopeParameter?.type?.isNullable() == true) {
                    val irSafeAs = IrTypeOperatorCallImpl(
                        startOffset,
                        endOffset,
                        scopeType,
                        IrTypeOperator.SAFE_CAST,
                        scopeType,
                        builderWithScope.irGet(dispatchReceiverParameter)
                    )

                    arguments[index] = irSafeAs
                }
//                                irAs(irGet(dispatchReceiverParameter), coroutineScopeRef.defaultType)
            }
        }
    }
}
