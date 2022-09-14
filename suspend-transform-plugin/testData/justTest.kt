// SOURCE
// FILE: Main.kt [MainKt#main]
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class JustTest { //  : ITest
    @JvmBlocking
    @JvmAsync
    suspend fun value(): Int = value("111")

    @JvmBlocking
    @JvmAsync
    suspend fun value(value: String): Int = value.toInt()
}

fun main() {
    println(JustTest().valueBlocking())
}