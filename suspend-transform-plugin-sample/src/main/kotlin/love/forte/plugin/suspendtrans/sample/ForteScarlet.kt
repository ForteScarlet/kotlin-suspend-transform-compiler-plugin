package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking


/**
 *
 * @author ForteScarlet
 */
class ForteScarlet {

    @JvmBlocking
    @JvmAsync
    suspend fun stringToInt(value: String): Int {
        delay(5)
        return value.toInt()
    }

}