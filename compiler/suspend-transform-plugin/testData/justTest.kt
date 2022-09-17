// SOURCE
// FILE: Main.kt [MainKt#main]
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

@kotlin.annotation.Target(AnnotationTarget.FUNCTION)
annotation class Hi

@JvmBlocking
suspend fun hello(): String = "hello"

@JvmBlocking
@JvmAsync
class JustTest : ITest { //  : ITest

    @Hi
    suspend fun value(): Int = 111

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    override suspend fun self(): JustTest = this
}

@JvmBlocking(asProperty = true)
@JvmAsync(asProperty = true)
interface ITest {
    suspend fun self(): ITest
}

fun main() {
    //println(JustTest().valueBlocking())
}