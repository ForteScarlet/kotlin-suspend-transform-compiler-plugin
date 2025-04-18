import example.MyClass
import example.MyInterface
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 *
 * @author ForteScarlet
 */
class MyClassReflectTests {

    @Test
    fun testMyClassReflect() {
        val acceptBlocking = MyClass::class.functions.find { it.name == "acceptBlocking" }
        assertNotNull(acceptBlocking)
        println(acceptBlocking)
        println(acceptBlocking.javaMethod)

        val deleteBlocking = MyInterface::class.functions.find { it.name == "deleteBlocking" }
        assertNotNull(deleteBlocking)
        println(deleteBlocking)
        println(deleteBlocking.javaMethod)
    }
}