import kotlinx.coroutines.cancel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import example.JvmReactiveScopedSamples
import example.JvmReactiveSamples
import example.MyClass
import example.MyInterface
import example.NullmarkModeSamples
import love.forte.plugin.suspendtrans.annotation.Api4J
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KType
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
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

    @OptIn(Api4J::class)
    @Test
    fun testJvmReactiveReflect() {
        val functions = JvmReactiveSamples::class.functions.associateBy { it.name }

        fun publisherArgumentType(functionName: String): KType {
            val returnType = requireNotNull(functions[functionName]) {
                "Function $functionName was not generated"
            }.returnType

            assertEquals(Publisher::class, returnType.classifier)
            return requireNotNull(returnType.arguments.single().type) {
                "Function $functionName has no Publisher generic argument"
            }
        }

        with(publisherArgumentType("stringValueReactive")) {
            assertEquals("kotlin.String", toString())
            assertFalse(isMarkedNullable)
        }

        with(publisherArgumentType("nullableStringValueReactive")) {
            assertEquals("kotlin.String", toString())
            assertFalse(isMarkedNullable)
        }

        with(publisherArgumentType("nullableBoundGenericValueReactive")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }

        with(publisherArgumentType("whereGenericValueReactive")) {
            assertTrue(toString().contains("&") && toString().contains("Any"), toString())
            assertFalse(isMarkedNullable)
        }

        with(JvmReactiveSamples::class.memberProperties.single { it.name == "nullablePropertyValueReactive" }) {
            assertEquals(Publisher::class, returnType.classifier)
            val argumentType = requireNotNull(returnType.arguments.single().type)
            assertEquals("kotlin.String", argumentType.toString())
            assertFalse(argumentType.isMarkedNullable)
        }

        val samples = JvmReactiveSamples()
        assertEquals("value", runBlocking { samples.stringValueReactive().awaitFirstOrNull() })
        assertEquals(null, runBlocking { samples.nullableStringValueReactive().awaitFirstOrNull() })

        val scopedSamples = JvmReactiveScopedSamples()
        try {
            assertEquals("scoped", runBlocking { scopedSamples.scopedValueReactive().awaitFirstOrNull() })
        } finally {
            scopedSamples.cancel()
        }
    }
}
