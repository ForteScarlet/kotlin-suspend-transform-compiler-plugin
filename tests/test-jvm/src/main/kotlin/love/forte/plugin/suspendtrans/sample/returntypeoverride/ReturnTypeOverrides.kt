package love.forte.plugin.suspendtrans.sample.returntypeoverride

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.suspendtrans.test.runner.JvmResultAsync
import love.forte.suspendtrans.test.runner.JvmResultBlock


interface Foo {
    @JvmBlocking
    @JvmAsync
    suspend fun <T> classic(value: T): T

    @JvmBlocking
    @JvmAsync
    suspend fun <T> classicResult(value: T): Result<T>

    @JvmResultBlock<String>
    @JvmResultAsync<String>
    suspend fun hello(): Result<String>

    @JvmResultBlock<T>
    @JvmResultAsync<T>
    suspend fun <T> foo(value: T): Result<T>

    @JvmResultBlock<T>
    @JvmResultAsync<T>
    suspend fun <T> fooError(value: T): Result<T>
}