@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

import love.forte.plugin.suspendtrans.annotation.JsPromise

/**
 *
 * @author ForteScarlet
 */
interface FooInterface {
    @JsExport.Ignore
    suspend fun run1(value: Int): String

    @JsExport.Ignore
    suspend fun run2(): String
}

class FooImpl : FooInterface {
    @JsExport.Ignore
    @JsPromise
    override suspend fun run1(value: Int): String = value.toString()

    @JsExport.Ignore
    @JsPromise // TODO asProperty
    override suspend fun run2(): String = ""
}

/**
 *
 * @author ForteScarlet
 */
interface FooInterface2 {
    @JsExport.Ignore
    @JsPromise
    suspend fun run1(value: Int): String

    @JsExport.Ignore
    @JsPromise // TODO asProperty
    suspend fun run2(): String
}


class FooImpl2 : FooInterface2 {
    @JsExport.Ignore
    @JsPromise
    override suspend fun run1(value: Int): String = value.toString()

    @JsExport.Ignore
    @JsPromise // TODO asProperty
    override suspend fun run2(): String = ""
}


/**
 *
 * @author ForteScarlet
 */
interface FooInterface3 {
    @JsExport.Ignore
    suspend fun run1(value: Int): String

    fun run1Blocking(value: Int): String = value.toString()

    @JsExport.Ignore
    @JsName("suspend_run2")
    suspend fun run2(): String

    val run2: String get() = ""
}


class FooImpl3 : FooInterface3 {
    @JsExport.Ignore
    @JsPromise
    override suspend fun run1(value: Int): String = value.toString()

    @JsExport.Ignore
    @JsPromise // asProperty
    override suspend fun run2(): String = ""
}
