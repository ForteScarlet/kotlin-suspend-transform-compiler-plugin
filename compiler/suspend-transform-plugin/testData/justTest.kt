// SOURCE
// FILE: Main.kt [MainKt#main]
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class JustTest : ITest { //  : ITest
    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun value(): Int = value("111")

    @JvmBlocking
    @JvmAsync
    suspend fun value(value: String): Int = value.toInt()

    @JvmBlocking
    @JvmAsync
    override suspend fun run99(name: String): Int = 1

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    override suspend fun run2(): Int = 1
}

interface ITest {
    @JvmBlocking
    @JvmAsync
    suspend fun run99(name: String = "forte"): Int

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun run2(): Int
}

fun main() {
    //println(JustTest().valueBlocking())
}