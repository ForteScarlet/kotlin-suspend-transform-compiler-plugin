@file:OptIn(ExperimentalJsExport::class)

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JsPromise
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@JsExport
abstract class IForteScarlet {
    @JsPromise
    @JsExport.Ignore
    abstract suspend fun stringToInt(value: String): Int
}


/**
 *
 * @author ForteScarlet
 */
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
class ForteScarlet : CoroutineScope, IForteScarlet() {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

    @JsPromise
    @JsExport.Ignore
    override suspend fun stringToInt(value: String): Int {
        delay(5)
        return value.toInt()
    }
}
