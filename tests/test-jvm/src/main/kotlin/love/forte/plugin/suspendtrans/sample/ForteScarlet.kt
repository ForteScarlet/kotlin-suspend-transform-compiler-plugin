package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.coroutines.CoroutineContext


abstract class IForteScarlet {
    @JvmAsync
    @JvmBlocking
    abstract suspend fun stringToInt(value: String): Int

    @JvmAsync(asProperty = true)
    @JvmBlocking(asProperty = true)
    abstract suspend fun value(): Int
}


/**
 *
 * @author ForteScarlet
 */
class ForteScarlet : CoroutineScope, IForteScarlet() {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @JvmAsync
    @JvmBlocking
    override suspend fun stringToInt(value: String): Int {
        delay(5)
        return value.toInt()
    }

    @JvmAsync(asProperty = true)
    @JvmBlocking(asProperty = true)
    override suspend fun value(): Int {
        delay(1)
        return 1
    }

    //    @JvmAsync
//    suspend fun stringToInt(value: String): Int {
//        delay(5)
//        return value.toInt()
//    }

}

fun main() {
    for (method in ForteScarlet::class.java.declaredMethods) {
        println(method)
    }

//    println("b stringToInt: " + ForteScarlet().stringToIntBlocking("1"))
//    println("a stringToInt: " + ForteScarlet().stringToIntAsync("1"))
//
//    println("b value: " + ForteScarlet().valueBlocking)
//    println("a value: " + ForteScarlet().valueAsync)
}
