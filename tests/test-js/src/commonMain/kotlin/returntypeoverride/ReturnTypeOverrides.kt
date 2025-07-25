package returntypeoverride

import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.suspendtrans.test.runner.JsResultPromise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Foo {
    @JsPromise
    @JsExport.Ignore
    suspend fun <T> classic(value: T): T

    @JsPromise
    @JsExport.Ignore
    suspend fun <T> classicResult(value: T): Result<T>

    @JsResultPromise<String>
    @JsExport.Ignore
    suspend fun hello(): Result<String>

    @JsResultPromise<T>
    @JsExport.Ignore
    suspend fun <T> foo(value: T): Result<T>

    @JsResultPromise<T>
    @JsExport.Ignore
    suspend fun <T> fooError(value: T): Result<T>
}