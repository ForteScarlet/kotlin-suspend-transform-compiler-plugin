import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JsPromise


class JsFoo {
    
    @JsPromise
    suspend fun getValue(): Int {
        delay(5)
        return 6
    }
    
}

suspend fun main() {
    
    val foo = JsFoo()
    println(foo)
    println(foo.getValue())
    
    // val p: Promise<Int> = foo.getValueAsync()
    
    
}