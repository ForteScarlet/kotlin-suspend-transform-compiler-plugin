package markname

import love.forte.plugin.suspendtrans.annotation.JsPromise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface MarkNameTestInterface {
    @JsPromise(markName = "foo")
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(markName = "foo")
interface MarkNameOnTypeTestInterface {
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(markName = "foo")
interface MarkNameSubOverwriteTestInterface {
    @JsName("_foo")
    @JsExport.Ignore
    @JsPromise(markName = "foo1")
    suspend fun foo(): Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface MarkNameTestInterfaceAsProperty {
    @JsPromise(asProperty = true, markName = "foo")
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(asProperty = true, markName = "foo")
interface MarkNameOnTypeTestInterfaceAsProperty {
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(markName = "foo")
interface MarkNameSubOverwriteTestInterfaceAsProperty {
    @JsName("_foo")
    @JsExport.Ignore
    @JsPromise(asProperty = true, markName = "foo1")
    suspend fun foo(): Int
}
