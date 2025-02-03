package love.forte.plugin.suspendtrans.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext
import kotlin.js.Promise
import kotlinx.coroutines.SupervisorJob

private val CoroutineContext4Js: CoroutineContext = Dispatchers.Default + SupervisorJob()

private val CoroutineScope4Js: CoroutineScope = CoroutineScope(CoroutineContext4Js)


@Suppress("FunctionName")
@Deprecated("Just for compile plugin.", level = DeprecationLevel.HIDDEN)
public fun <T, F : suspend () -> T> `$runInAsync$`(
    block: F,
    scope: CoroutineScope? = null
): Promise<T> {
    return (scope ?: CoroutineScope4Js).promise { block.invoke() }
}
