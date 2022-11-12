// SOURCE
// FILE: Main.kt [MainKt#main]
import kotlinx.coroutines.runBlocking
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Foo {
    @JvmAsync
    suspend fun foo(): String = ""
}

class Bar : CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    @JvmAsync
    suspend fun bar(): String = ""
}