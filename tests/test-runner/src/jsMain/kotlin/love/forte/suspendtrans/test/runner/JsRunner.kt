package love.forte.suspendtrans.test.runner

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise


@OptIn(DelicateCoroutinesApi::class)
fun <T> jsResultToAsync(block: suspend () -> Result<T>): Promise<T> {
    return GlobalScope.promise { block().getOrThrow() }
}