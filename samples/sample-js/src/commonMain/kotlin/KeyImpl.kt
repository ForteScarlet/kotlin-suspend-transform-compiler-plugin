import love.forte.plugin.suspendtrans.annotation.JsPromise

/**
 *
 * @author ForteScarlet
 */
expect class KeyImpl : Key {
    @JsPromise
    override suspend fun run(): ByteArray
}
