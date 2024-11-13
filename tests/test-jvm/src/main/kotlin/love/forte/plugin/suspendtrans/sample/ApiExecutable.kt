package love.forte.plugin.suspendtrans.sample

import love.forte.plugin.suspendtrans.annotation.JvmAsync

interface Api<T : Any> {
    suspend fun run(): T
}

interface ApiResult<T : Any>

/**
 *
 * @author ForteScarlet
 */
interface ApiExecutable {
    @JvmAsync
    fun <T : Any> execute(api: Api<T>): T // ApiResult<T>
}
