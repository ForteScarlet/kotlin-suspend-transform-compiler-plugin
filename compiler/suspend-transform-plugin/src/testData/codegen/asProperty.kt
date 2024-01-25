// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

annotation class TA(val value: String = "forte")

class PropFoo {
    @love.forte.plugin.suspendtrans.annotation.JvmBlocking(asProperty = true, suffix = "")
    @love.forte.plugin.suspendtrans.annotation.JvmAsync(asProperty = true)
    @TA(value = "forliy")
    suspend fun prop(): String = ""

    @JvmBlocking
    @JvmAsync
    suspend fun age(): Int = 1
}
