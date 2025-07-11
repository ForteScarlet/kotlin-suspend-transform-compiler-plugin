// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.plugin.suspendtrans.annotation.JvmBlockingWithType
import love.forte.plugin.suspendtrans.annotation.JvmBlockingWithType0

annotation class FooAnno<T>

class Foo {
    @JvmBlockingWithType<T>
    @JvmBlockingWithType0<T>
    @FooAnno<T>
    suspend fun <T> foo(): kotlin.Result<T> = Result.success("foo")
}
