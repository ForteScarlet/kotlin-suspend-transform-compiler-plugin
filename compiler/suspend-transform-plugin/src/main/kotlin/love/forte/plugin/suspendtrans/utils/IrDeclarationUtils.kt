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

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.ir.util.isInterface as isInterfaceIr


fun IrPluginContext.createIrBuilder(
    symbol: IrSymbol,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
) = DeclarationIrBuilder(this, symbol, startOffset, endOffset)


fun IrType.isClassType(fqName: FqNameUnsafe, hasQuestionMark: Boolean? = null): Boolean {
    if (this !is IrSimpleType) return false
    if (hasQuestionMark != null && this.isMarkedNullable() != hasQuestionMark) return false
    return classifier.isClassWithFqName(fqName)
}

fun IrDeclarationContainer.computeModality(originFunction: IrFunction): Modality {
    if (isInterface) return Modality.OPEN
    
    return when {
        originFunction.isFinal -> Modality.FINAL
        isOpen || isAbstract || isSealed -> Modality.OPEN
        else -> Modality.FINAL
    }
}

val IrDeclarationContainer.isInterface: Boolean
    get() = (this as? IrClass)?.isInterfaceIr == true

val IrDeclarationContainer.isAbstract: Boolean
    get() = (this as? IrClass)?.modality == Modality.ABSTRACT

val IrDeclarationContainer.isOpen: Boolean
    get() = (this as? IrClass)?.modality == Modality.OPEN

val IrDeclarationContainer.isSealed: Boolean
    get() = (this as? IrClass)?.modality == Modality.SEALED

val IrFunction.isFinal: Boolean
    get() = (this as? IrSimpleFunction)?.modality == Modality.FINAL
