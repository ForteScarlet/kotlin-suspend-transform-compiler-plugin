import kotlinx.coroutines.delay
import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.random.Random


/**
 *
 * @author ForteScarlet
 */
class Foo {
    
    @JvmBlocking
    @JvmAsync
    @JsPromise
    suspend fun waitAndGetValue(): Long {
        delay(5)
        return Random.nextLong()
    }
    
}