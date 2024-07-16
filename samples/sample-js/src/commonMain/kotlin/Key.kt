@file:OptIn(ExperimentalJsExport::class)

import love.forte.plugin.suspendtrans.annotation.JsPromise


/**
 *
 * @author ForteScarlet
 */
@JsExport
abstract class Key {

    @JsPromise
    @JsExport.Ignore
    abstract suspend fun run(): Any

}
