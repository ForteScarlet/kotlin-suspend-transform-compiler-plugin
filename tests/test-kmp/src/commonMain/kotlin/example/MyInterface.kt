package example

import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 *
 * @author ForteScarlet
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
interface MyInterface {
    @JvmBlocking
    @JvmAsync
    @JsPromise
    @JsExport.Ignore
    suspend fun delete()
    @JvmBlocking
    @JvmAsync
    @JsPromise(baseName = "delete2")
    @JsExport.Ignore
    suspend fun delete(vararg value: String)
}