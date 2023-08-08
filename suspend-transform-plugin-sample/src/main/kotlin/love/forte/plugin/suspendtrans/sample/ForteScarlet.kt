package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


abstract class IForteScarlet {
    @JvmAsync
    abstract suspend fun stringToInt(value: String): Int
}


/**
 *
 * @author ForteScarlet
 */
class ForteScarlet : CoroutineScope, IForteScarlet() {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

    @JvmAsync
    override suspend fun stringToInt(value: String): Int {
        delay(5)
        return value.toInt()
    }

    //    @JvmAsync
//    suspend fun stringToInt(value: String): Int {
//        delay(5)
//        return value.toInt()
//    }

}
