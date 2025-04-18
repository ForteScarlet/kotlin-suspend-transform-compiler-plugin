package example

import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

expect class MoneyValue

@OptIn(ExperimentalJsExport::class)
@JsExport
class MyClass {
    @JvmBlocking
    @JvmAsync
    @JsPromise
    @JsExport.Ignore
    suspend fun errorReproduction(amount: MoneyValue) = println(amount)

    @JvmBlocking
    @JvmAsync
    @JsExport.Ignore
    suspend fun accept(): Int {
        delay(1)
        return 1
    }
}
