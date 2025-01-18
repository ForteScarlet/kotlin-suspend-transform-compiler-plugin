// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

public interface DeleteOption

abstract class MyClass {
    @JvmBlocking
    @JvmAsync
    suspend open fun deleteAll(vararg options: DeleteOption): Int {
        return 1
    }
}
