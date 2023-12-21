@file:OptIn(ExperimentalJsExport::class)

import wasmtrans.JsPromise


abstract class IForteScarlet {
    @JsPromise
    abstract suspend fun stringToInt(value: String): Int
}


/**
 *
 * @author ForteScarlet
 */
class ForteScarlet : IForteScarlet() {
    @JsPromise
    @JsExport.Ignore
    override suspend fun stringToInt(value: String): Int {
        return value.toInt()
    }
}
