package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


abstract class IForteScarlet {
    @JvmAsync
    @JvmBlocking
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
    @JvmBlocking
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

//fun main() {
//    for (method in ForteScarlet::class.java.declaredMethods) {
//        println(method)
//    }
//}
