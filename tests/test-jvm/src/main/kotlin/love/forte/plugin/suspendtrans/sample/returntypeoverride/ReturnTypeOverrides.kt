package love.forte.plugin.suspendtrans.sample.returntypeoverride

import kotlinx.coroutines.delay
import love.forte.suspendtrans.test.runner.JvmResultAsync
import love.forte.suspendtrans.test.runner.JvmResultBlock

interface Foo {
    @JvmResultBlock<String>
    @JvmResultAsync<String>
    suspend fun hello(): Result<String> {
        delay(1)
        return Result.success("Hello, ")
    }

    // TODO e: file://~/suspend-transform-kotlin-compile-plugin/tests/test-jvm/src/main/kotlin/love/forte/plugin/suspendtrans/sample/returntypeoverride/ReturnTypeOverrides.kt:16:21 Unresolved reference 'T'.
    // @JvmResultBlock<T>
    // @JvmResultAsync<T>
    // suspend fun <T> foo(value: T): Result<T> {
    //     delay(1)
    //     return Result.success(value)
    // }

}