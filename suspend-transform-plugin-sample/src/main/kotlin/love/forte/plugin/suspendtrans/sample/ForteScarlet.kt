package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking


/**
 *
 * @author ForteScarlet
 */
class ForteScarlet : Scarlet {
    @JvmBlocking
    @JvmAsync
    suspend fun stringToInt(value: String): Int {
        delay(5)
        return value.toInt()
    }

    @JvmBlocking
    @JvmAsync
    override suspend fun name(): String {
        delay(5)
        return "ForteScarlet"
    }

    override suspend fun age(): Long {
        delay(5)
        return 114514
    }

    @JvmBlocking
    @JvmAsync
    override suspend fun self(): ForteScarlet {
        delay(5)
        return this
    }


    override suspend fun self2(): ForteScarlet {
        delay(5)
        return this
    }
}

interface Scarlet {

    @JvmBlocking
    @JvmAsync
    suspend fun name(): String

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun age(): Long

    @JvmBlocking
    @JvmAsync
    suspend fun self(): Scarlet

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun self2(): Scarlet


}