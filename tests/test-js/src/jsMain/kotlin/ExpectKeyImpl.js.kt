import love.forte.plugin.suspendtrans.annotation.JsPromise

@OptIn(ExperimentalJsExport::class)
@JsExport
actual class KeyImpl : Key() {

    @JsExport.Ignore
    @JsPromise
    actual override suspend fun run(): ByteArray {
        TODO("Not yet implemented")
    }
}
