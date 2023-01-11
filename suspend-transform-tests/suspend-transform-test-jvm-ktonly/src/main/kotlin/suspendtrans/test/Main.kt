package suspendtrans.test

import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import suspendtrans.test.AB
import java.util.concurrent.CompletableFuture


class JvmFoo {
    
    @AB
    suspend fun getValue(): Int {
        delay(5)
        return 6
    }
    
    @AB(blockingSuffix = "Bk", asyncSuffix = "Ay")
    suspend fun getValue2(): Int {
        delay(5)
        return 6
    }
    
}

//suspend fun main() {
//    val foo = JvmFoo()
//    foo.getValueBlocking()
//
//    println(foo.getValue())
//    println(foo.getValueBlocking())
//    println(foo.getValueAsync())
//    println(foo.getValueAsync()::class)
//    val future: CompletableFuture<*> = foo.getValueAsync()
//    future.thenAccept {
//        println("Future accept: $it")
//        println("Future accept: ${it::class}")
//    }
//
//    delay(50)
//    // OK
//}
