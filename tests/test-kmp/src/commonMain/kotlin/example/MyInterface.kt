package example

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

/**
 *
 * @author ForteScarlet
 */
interface MyInterface {
    @JvmBlocking
    @JvmAsync
    suspend fun delete()
    @JvmBlocking
    @JvmAsync
    suspend fun delete(vararg value: String)
}