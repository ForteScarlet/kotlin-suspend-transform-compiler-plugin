import example.MyClass
import example.MyInterface
import example.NullmarkModeSamples
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KType
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

    @Test
    fun testNullmarkModeReflect() {
        val functions = NullmarkModeSamples::class.functions.associateBy { it.name }

        fun futureArgumentType(functionName: String): KType {
            val returnType = requireNotNull(functions[functionName]) {
                "Function $functionName was not generated"
            }.returnType

            assertEquals(CompletableFuture::class, returnType.classifier)
            return requireNotNull(returnType.arguments.single().type) {
                "Function $functionName has no CompletableFuture generic argument"
            }
        }

        with(futureArgumentType("stringValueNullableAsync")) {
            assertEquals("kotlin.String?", toString())
            assertTrue(isMarkedNullable)
        }

        with(futureArgumentType("genericValueNullableAsync")) {
            assertTrue(toString().endsWith("?"), toString())
            assertTrue(isMarkedNullable)
        }

        with(futureArgumentType("whereGenericValueNullableAsync")) {
            assertTrue(toString().endsWith("?"), toString())
            assertTrue(isMarkedNullable)
        }

        with(futureArgumentType("nullableStringValueNonNullAsync")) {
            assertEquals("kotlin.String", toString())
            assertFalse(isMarkedNullable)
        }

        with(futureArgumentType("nullableBoundGenericValueNonNullAsync")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }

        with(futureArgumentType("nullableWhereGenericValueNonNullAsync")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }
    }
}
