// SOURCE
// FILE: Main.kt [MainKt#main]
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

@kotlin.annotation.Target(AnnotationTarget.FUNCTION)
annotation class Hi

class JustTest : ITest { //  : ITest

    @Hi
    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun value(): Int = value("111")

    @JvmBlocking
    @JvmAsync
    suspend fun value(value: String): Int = value.toInt()

    @JvmBlocking
    @JvmAsync
    //@kotlin.jvm.JvmOverload
    override suspend fun run99(name: String): Int = 1

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    override suspend fun run2(): Int = 1


    @JvmBlocking
    @JvmAsync
    override suspend fun self(): JustTest = this

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    override suspend fun self2(): JustTest = this
}

interface ITest {
    @JvmBlocking
    @JvmAsync
    suspend fun run99(name: String = "forte"): Int

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun run2(): Int

    @JvmBlocking
    @JvmAsync
    suspend fun self(): ITest

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun self2(): ITest
}

fun main() {
    //println(JustTest().valueBlocking())
}