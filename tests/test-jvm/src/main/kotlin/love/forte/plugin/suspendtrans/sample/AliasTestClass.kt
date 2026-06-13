/*
 * Copyright (c) 2026 Forte Scarlet
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

package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.suspendCancellableCoroutine
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import java.math.BigDecimal
import kotlin.coroutines.resume

typealias MoneyValue = Long
typealias BigMoneyValue = BigDecimal

class AliasTestClass {
    @JvmBlocking
    @JvmAsync
    suspend fun errorReproduction1(amount: MoneyValue): MoneyValue = suspendCancellableCoroutine {
        it.resume(amount)
    }

    @JvmBlocking
    @JvmAsync
    suspend fun errorReproduction2(amount: BigMoneyValue): BigMoneyValue = suspendCancellableCoroutine {
        it.resume(amount)
    }
}