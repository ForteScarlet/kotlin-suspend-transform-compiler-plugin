package markname

import love.forte.plugin.suspendtrans.annotation.JsPromise

@Suppress("RedundantSuspendModifier")
@OptIn(ExperimentalJsExport::class)
@JsExport
class MarkNameTestClass {
    @JsPromise(markName = "foo")
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int = 1

    @JsName("aName")
    fun named(): String = ""
}

@Suppress("RedundantSuspendModifier")
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(markName = "foo")
class MarkNameOnTypeTestClass {
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int = 1
}

@Suppress("RedundantSuspendModifier")
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(markName = "foo")
class MarkNameSubOverwriteTestClass {
    @JsPromise(markName = "foo1")
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int = 1
}

@Suppress("RedundantSuspendModifier")
@OptIn(ExperimentalJsExport::class)
@JsExport
class MarkNameTestClassAsProperty {
    @JsPromise(asProperty = true, markName = "foo")
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int = 1

    val named: String
        @JsName("aName")
        get() = ""
}

@Suppress("RedundantSuspendModifier")
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(asProperty = true, markName = "foo")
class MarkNameOnTypeTestClassAsProperty {
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int = 1
}

@Suppress("RedundantSuspendModifier")
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsPromise(markName = "foo")
class MarkNameSubOverwriteTestClassAsProperty {
    @JsPromise(asProperty = true, markName = "foo1")
    @JsName("_foo")
    @JsExport.Ignore
    suspend fun foo(): Int = 1
}
