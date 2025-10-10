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

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType

fun ValueParameterDescriptor.copy(
    containingDeclaration: CallableDescriptor = this.containingDeclaration,
    original: ValueParameterDescriptor? = this.original,
    index: Int = this.index,
    annotations: Annotations = this.annotations,
    name: Name = this.name,
    outType: KotlinType = this.type,
    declaresDefaultValue: Boolean = this.declaresDefaultValue(),
    isCrossinline: Boolean = this.isCrossinline,
    isNoinline: Boolean = this.isNoinline,
    varargElementType: KotlinType? = this.varargElementType,
    source: SourceElement = this.source
): ValueParameterDescriptor {
    return ValueParameterDescriptorImpl(
        containingDeclaration,
        original,
        index,
        annotations,
        name,
        outType,
        declaresDefaultValue,
        isCrossinline,
        isNoinline,
        varargElementType,
        source,
    )
}



