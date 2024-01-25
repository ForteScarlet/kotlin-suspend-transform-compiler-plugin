// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
//import kotlinx.coroutines.CoroutineScope
//import kotlin.coroutines.CoroutineContext
//import kotlin.coroutines.EmptyCoroutineContext

class BasicFoo {
    @JvmAsync
    suspend fun foo(): String = ""
}

class BasicBar
//    : CoroutineScope
{
    //override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    @JvmAsync
    suspend fun bar(): String = ""
    @JvmAsync
    suspend fun bar(i: Int): String = ""
}

interface InterfaceBar {
    @JvmAsync
    suspend fun bar(): String
    @JvmAsync
    suspend fun bar(i: Int): String
}
