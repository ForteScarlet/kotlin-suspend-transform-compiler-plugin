package returntypeoverride

import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.js.Promise
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/**
 *
 * @author ForteScarlet
 */
class JsReturnTypeOverrideTests {
    class TestErr : RuntimeException()

    class FooImpl : Foo {
        override suspend fun <T> classic(value: T): T = value
        override suspend fun <T> classicResult(value: T): Result<T> = Result.success(value)
        override suspend fun hello(): Result<String> = Result.success("Hello, World")
        override suspend fun <T> foo(value: T): Result<T> = Result.success(value)
        override suspend fun <T> fooError(value: T): Result<T> = Result.failure(TestErr())
    }

    private lateinit var fooImpl: FooImpl

    @BeforeTest
    fun beforeEach() {
        fooImpl = FooImpl()
    }

    private class TestObj

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFooClassic() = runTest {
        val obj = TestObj()
        val asyncResult = fooImpl.asDynamic().classicAsync(obj)
            .unsafeCast<Promise<TestObj>>()

        assertSame(obj, asyncResult.await())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFooClassicResult() = runTest {
        val obj = TestObj()
        val asyncResult = fooImpl.asDynamic().classicResultAsync(obj)
            .unsafeCast<Promise<Result<TestObj>>>()

        assertSame(obj, asyncResult.await().getOrNull())
    }

    @Test
    fun testFooHello() = runTest {
        val asyncResult = fooImpl.asDynamic().helloAsync()
            .unsafeCast<Promise<String>>()

        assertSame("Hello, World", asyncResult.await())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFooFoo() = runTest {
        val obj = TestObj()
        val asyncResult = fooImpl.asDynamic().fooAsync(obj)
            .unsafeCast<Promise<TestObj>>()

        assertSame(obj, asyncResult.await())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFooError() = runTest {
        val obj = TestObj()
        val asyncResult = fooImpl.asDynamic().fooErrorAsync(obj)
            .unsafeCast<Promise<TestObj>>()

        assertFailsWith<TestErr> {
            asyncResult.await()
        }
    }


}