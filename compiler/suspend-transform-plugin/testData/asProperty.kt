// SOURCE
// FILE: Main.kt [MainKt#main]
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking


class Foo {
    @JvmBlocking(asProperty = true, suffix = "")
    @JvmAsync(asProperty = true)
    suspend fun name(): String = ""

    @JvmBlocking
    @JvmAsync
    suspend fun age(): Int = 1
}
