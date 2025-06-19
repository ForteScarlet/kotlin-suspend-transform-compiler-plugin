package markname

import love.forte.plugin.suspendtrans.sample.markname.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@Suppress("UNCHECKED_CAST")
class MarkNameTests {
    @Test
    fun testMarkNameClass() {
        val clz = MarkNameTestClass()
        val blockMethod = MarkNameTestClass::class.java.getMethod("fooB")
        assertEquals(Int::class.javaPrimitiveType, blockMethod.returnType)
        assertEquals(1, blockMethod.invoke(clz))
        val asyncMethod = MarkNameTestClass::class.java.getMethod("fooA")
        assertEquals(CompletableFuture::class.java, asyncMethod.returnType)
        val asyncResult = asyncMethod.invoke(clz) as CompletableFuture<Int>
        assertEquals(1, asyncResult.join())
    }


    @Test
    fun testMarkNameClassOnType() {
        val clz = MarkNameOnTypeTestClass()
        val blockMethod = MarkNameOnTypeTestClass::class.java.getMethod("fooB")
        assertEquals(Int::class.javaPrimitiveType, blockMethod.returnType)
        assertEquals(1, blockMethod.invoke(clz))
        val asyncMethod = MarkNameOnTypeTestClass::class.java.getMethod("fooA")
        assertEquals(CompletableFuture::class.java, asyncMethod.returnType)
        val asyncResult = asyncMethod.invoke(clz) as CompletableFuture<Int>
        assertEquals(1, asyncResult.join())
    }

    @Test
    fun testMarkNameClassSubOverwrite() {
        val clz = MarkNameSubOverwriteTestClass()
        val blockMethod = MarkNameSubOverwriteTestClass::class.java.getMethod("fooB1")
        assertEquals(Int::class.javaPrimitiveType, blockMethod.returnType)
        assertEquals(1, blockMethod.invoke(clz))
        val asyncMethod = MarkNameSubOverwriteTestClass::class.java.getMethod("fooA1")
        assertEquals(CompletableFuture::class.java, asyncMethod.returnType)
        val asyncResult = asyncMethod.invoke(clz) as CompletableFuture<Int>
        assertEquals(1, asyncResult.join())
    }

    @Test
    fun testMarkNameClassAsProperty() {
        val clz = MarkNameTestClassAsProperty()
        val blockProp = MarkNameTestClassAsProperty::class.memberProperties.find { it.name == "fooBlocking" }
        assertNotNull(blockProp)
        val blockMethod = blockProp.javaGetter
        assertNotNull(blockMethod)
        assertEquals("fooB", blockMethod.name)
        assertEquals(Int::class.javaPrimitiveType, blockMethod.returnType)
        assertEquals(1, blockMethod.invoke(clz))

        val asyncProp = MarkNameTestClassAsProperty::class.memberProperties.find { it.name == "fooAsync" }
        assertNotNull(asyncProp)
        val asyncMethod = asyncProp.javaGetter
        assertNotNull(asyncMethod)
        assertEquals("fooA", asyncMethod.name)
        assertEquals(CompletableFuture::class.java, asyncMethod.returnType)
        val asyncResult = asyncMethod.invoke(clz) as CompletableFuture<Int>
        assertEquals(1, asyncResult.join())
    }

    @Test
    fun testMarkNameClassOnTypeAsProperty() {
        val clz = MarkNameOnTypeTestClassAsProperty()
        val blockProp = MarkNameOnTypeTestClassAsProperty::class.memberProperties.find { it.name == "fooBlocking" }
        assertNotNull(blockProp)
        val blockMethod = blockProp.javaGetter
        assertNotNull(blockMethod)
        assertEquals("fooB", blockMethod.name)
        assertEquals(Int::class.javaPrimitiveType, blockMethod.returnType)
        assertEquals(1, blockMethod.invoke(clz))

        val asyncProp = MarkNameOnTypeTestClassAsProperty::class.memberProperties.find { it.name == "fooAsync" }
        assertNotNull(asyncProp)
        val asyncMethod = asyncProp.javaGetter
        assertNotNull(asyncMethod)
        assertEquals("fooA", asyncMethod.name)
        assertEquals(CompletableFuture::class.java, asyncMethod.returnType)
        val asyncResult = asyncMethod.invoke(clz) as CompletableFuture<Int>
        assertEquals(1, asyncResult.join())
    }

    @Test
    fun testMarkNameClassSubOverwriteAsProperty() {
        val clz = MarkNameSubOverwriteTestClassAsProperty()
        val blockProp =
            MarkNameSubOverwriteTestClassAsProperty::class.memberProperties.find { it.name == "fooBlocking" }
        assertNotNull(blockProp)
        val blockMethod = blockProp.javaGetter
        assertNotNull(blockMethod)
        assertEquals("fooB1", blockMethod.name)
        assertEquals(Int::class.javaPrimitiveType, blockMethod.returnType)
        assertEquals(1, blockMethod.invoke(clz))

        val asyncProp = MarkNameSubOverwriteTestClassAsProperty::class.memberProperties.find { it.name == "fooAsync" }
        assertNotNull(asyncProp)
        val asyncMethod = asyncProp.javaGetter
        assertNotNull(asyncMethod)
        assertEquals("fooA1", asyncMethod.name)
        assertEquals(CompletableFuture::class.java, asyncMethod.returnType)
        val asyncResult = asyncMethod.invoke(clz) as CompletableFuture<Int>
        assertEquals(1, asyncResult.join())
    }

}
