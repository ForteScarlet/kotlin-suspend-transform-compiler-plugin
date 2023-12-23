package love.forte.plugin.suspendtrans.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise

private val CoroutineContext4Js: CoroutineContext = EmptyCoroutineContext

private val CoroutineScope4Js: CoroutineScope = CoroutineScope(CoroutineContext4Js)


@Suppress("FunctionName")
//@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
public fun <T, F : suspend () -> T> `$runInAsync$`(
    block: F,
    scope: CoroutineScope? = null
): Promise<T> {
    println("block: $block")
    println("block.asDynamic().invoke: " + block.asDynamic().invoke)
    println("block::class: " + block::class)
    return (scope ?: CoroutineScope4Js).promise { block.invoke() }
}
