// SOURCE
// FILE: Main.kt [MainKt#main]
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class JustTest : ITest { //  : ITest
    @JvmBlocking
    @JvmAsync
    suspend fun value(): Int = value("111")

    @JvmBlocking
    @JvmAsync
    suspend fun value(value: String): Int = value.toInt()

    @JvmBlocking
    @JvmAsync
    override suspend fun run(name: String): Int = 1
}

interface ITest {
    @JvmBlocking
    @JvmAsync
    suspend fun run(name: String = "forte"): Int
}

fun main() {
    //println(JustTest().valueBlocking())
}