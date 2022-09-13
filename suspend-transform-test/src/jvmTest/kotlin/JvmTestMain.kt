import kotlinx.coroutines.runBlocking


/**
 *
 * @author ForteScarlet
 */
class JvmTestMain {
    
    @kotlin.test.Test
    fun run() = runBlocking {
        println(Foo().waitAndGetValue())
        Foo::class.java.declaredMethods.forEach {
            println("Method: $it")
        }
    }
}