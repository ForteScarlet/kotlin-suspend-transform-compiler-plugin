import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JsPromise
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


abstract class IForteScarlet {
    @JsPromise
    abstract suspend fun stringToInt(value: String): Int
}


/**
 *
 * @author ForteScarlet
 */
class ForteScarlet : CoroutineScope, IForteScarlet() {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

    @JsPromise
    override suspend fun stringToInt(value: String): Int {
        delay(5)
        return value.toInt()
    }
}
