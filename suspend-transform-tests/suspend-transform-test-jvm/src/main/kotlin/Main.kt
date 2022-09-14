package suspendtrans.test

import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import java.util.concurrent.CompletableFuture


class JvmFoo {
    
    @JvmBlocking
    @JvmAsync
    suspend fun getValue(): Int {
        delay(5)
        return 6
    }
    
}

suspend fun main() {
    val foo = JvmFoo()
    println(foo.getValue())
    println(foo.getValueBlocking())
    println(foo.getValueAsync())
    println(foo.getValueAsync()::class)
    val future: CompletableFuture<*> = foo.getValueAsync()
    future.thenAccept {
        println("Future accept: $it")
        println("Future accept: ${it::class}")
    }
    
    delay(50)
    // OK
}