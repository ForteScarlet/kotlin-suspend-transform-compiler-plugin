// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class Foo {
    @JvmName("_foo")
    @JvmAsync(markName = "fooA")
    @JvmBlocking(markName = "fooB")
    suspend fun foo(): String = "foo"
}
