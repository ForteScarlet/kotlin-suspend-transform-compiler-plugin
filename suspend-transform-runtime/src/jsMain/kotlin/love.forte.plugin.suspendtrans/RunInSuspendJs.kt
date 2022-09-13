package love.forte.plugin.suspendtrans

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import love.forte.plugin.suspendtrans.annotation.ExperimentalJsApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise

@ExperimentalJsApi
public var CoroutineContext4Js: CoroutineContext = EmptyCoroutineContext

@ExperimentalJsApi
public var CoroutineScope4Js: CoroutineScope = CoroutineScope(CoroutineContext4Js)


@Suppress("FunctionName")
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@ExperimentalJsApi
public fun <T> `$runInAsync$`(
    block: suspend () -> T,
): Promise<T> {
    return CoroutineScope4Js.promise { block() }
}