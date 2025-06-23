package markname

import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


class MarkNameTests {
    @Test
    fun testMarkNameClass() = runTest {
        val fooResult = MarkNameTestClass().asDynamic().foo()
        assertIs<Promise<Int>>(fooResult)
        assertEquals(1, fooResult.unsafeCast<Promise<Int>>().await())
    }

    @Test
    fun testMarkNameClassOnType() = runTest {
        val fooResult = MarkNameOnTypeTestClass().asDynamic().foo()
        assertIs<Promise<Int>>(fooResult)
        assertEquals(1, fooResult.unsafeCast<Promise<Int>>().await())
    }

    @Test
    fun testMarkNameClassSubOverwrite() = runTest {
        val fooResult = MarkNameSubOverwriteTestClass().asDynamic().foo1()
        assertIs<Promise<Int>>(fooResult)
        assertEquals(1, fooResult.unsafeCast<Promise<Int>>().await())
    }

    @Test
    fun testMarkNameInterface() = runTest {
        val fooResult = object : MarkNameTestInterface {
            override suspend fun foo(): Int = 1
        }.asDynamic().foo()
        assertIs<Promise<Int>>(fooResult)
        assertEquals(1, fooResult.unsafeCast<Promise<Int>>().await())
    }

    @Test
    fun testMarkNameInterfaceOnType() = runTest {
        val fooResult = object : MarkNameOnTypeTestInterface {
            override suspend fun foo(): Int = 1
        }.asDynamic().foo()
        assertIs<Promise<Int>>(fooResult)
        assertEquals(1, fooResult.unsafeCast<Promise<Int>>().await())
    }

    @Test
    fun testMarkNameInterfaceSubOverwrite() = runTest {
        val fooResult = object : MarkNameSubOverwriteTestInterface {
            override suspend fun foo(): Int = 1
        }.asDynamic().foo1()
        assertIs<Promise<Int>>(fooResult)
        assertEquals(1, fooResult.unsafeCast<Promise<Int>>().await())
    }

    // TODO see https://youtrack.jetbrains.com/issue/KT-78473
//    @Test
//    fun testMarkNameClassAsProperty() = runTest {
//        val fooResult = MarkNameTestClassAsProperty().asDynamic().foo
//        assertIs<Promise<Int>>(fooResult)
//        assertEquals(1, fooResult.unsafeCast<Promise<Int>>().await())
//    }
}
