package love.forte.plugin.suspendtrans.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise

private val CoroutineContext4Js: CoroutineContext = EmptyCoroutineContext

private val CoroutineScope4Js: CoroutineScope = CoroutineScope(CoroutineContext4Js)


@Suppress("FunctionName")
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
public fun <T> `$runInAsync$`(
    block: suspend () -> T,
): Promise<T> {
    return CoroutineScope4Js.promise(start = CoroutineStart.UNDISPATCHED) { block() }
}