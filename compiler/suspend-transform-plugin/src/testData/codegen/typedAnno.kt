// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.plugin.suspendtrans.annotation.JvmBlockingWithType

interface Inter {
    @JvmBlockingWithType<String>
    suspend fun runner(): kotlin.Result<String>

    @JvmBlockingWithType<T>
    suspend fun <T> runner1(): kotlin.Result<T>

    @JvmBlockingWithType<*>
    suspend fun runnerStar(): kotlin.Result<String>
}