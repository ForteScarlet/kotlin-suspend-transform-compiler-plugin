// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

typealias MoneyValue = Long
typealias JB = love.forte.plugin.suspendtrans.annotation.JvmBlocking
typealias JA = love.forte.plugin.suspendtrans.annotation.JvmAsync

class MyClass {
    @JvmBlocking
    @JvmAsync
    suspend fun errorReproduction(amount: MoneyValue) { println(amount) }

    @JB
    @JA
    suspend fun aliasFun() {}
}
