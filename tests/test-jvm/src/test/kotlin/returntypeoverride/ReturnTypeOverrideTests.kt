package returntypeoverride

import love.forte.plugin.suspendtrans.sample.returntypeoverride.Foo
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import kotlin.reflect.full.functions
import kotlin.test.*

/**
 *
 * @author ForteScarlet
 */
class ReturnTypeOverrideTests {
    private class TestErr : RuntimeException()

    private class FooImpl : Foo {
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

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFooClassic() {
        val classicBlockingFunction = FooImpl::class.functions.find { it.name == "classicBlocking" }
        assertNotNull(classicBlockingFunction, "function `classicBlocking` cannot be null")
        val classicBlockingFunctionResult = classicBlockingFunction.call(fooImpl, "Hello")
        assertIs<String>(classicBlockingFunctionResult)
        assertEquals("Hello", classicBlockingFunctionResult)

        val classicAsyncFunction = FooImpl::class.functions.find { it.name == "classicAsync" }
        assertNotNull(classicAsyncFunction, "function `classicAsync` cannot be null")
        val classicAsyncFunctionResult = classicAsyncFunction.call(fooImpl, "World") as CompletableFuture<String>
        assertIs<CompletableFuture<String>>(classicAsyncFunctionResult)
        assertEquals("World", classicAsyncFunctionResult.join())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFooClassicResult() {
        val blockingFunction = FooImpl::class.functions.find { it.name == "classicResultBlocking" }
        assertNotNull(blockingFunction, "function `classicResultBlocking` cannot be null")
        val blockingResult = blockingFunction.call(fooImpl, "Hello") as Result<String>
        assertIs<Result<String>>(blockingResult)
        assertEquals("Hello", blockingResult.getOrNull())

        val asyncFunction = FooImpl::class.functions.find { it.name == "classicResultAsync" }
        assertNotNull(asyncFunction, "function `classicResultAsync` cannot be null")
        val asyncResult = asyncFunction.call(fooImpl, "World") as CompletableFuture<Result<String>>
        assertIs<CompletableFuture<out Result<String>>>(asyncResult)
        assertEquals("World", asyncResult.join().getOrNull())
    }

    @Test
    fun testHello() {
        val blockingFunction = FooImpl::class.functions.find { it.name == "helloBlocking" }
        assertNotNull(blockingFunction, "function `helloBlocking` cannot be null")
        val blockingResult = blockingFunction.call(fooImpl) as String
        assertEquals("Hello, World", blockingResult)

        val asyncFunction = FooImpl::class.functions.find { it.name == "helloAsync" }
        assertNotNull(asyncFunction, "function `helloAsync` cannot be null")
        val asyncResult = asyncFunction.call(fooImpl)
        assertIs<CompletableFuture<out String>>(asyncResult)
        assertEquals("Hello, World", asyncResult.join())
    }

    private class TestObj

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFoo() {
        val obj = TestObj()
        val blockingFunction = FooImpl::class.functions.find { it.name == "fooBlocking" }
        assertNotNull(blockingFunction, "function `fooBlocking` cannot be null")
        val blockingResult = blockingFunction.call(fooImpl, obj)
        assertSame(obj, blockingResult)

        val asyncFunction = FooImpl::class.functions.find { it.name == "fooAsync" }
        assertNotNull(asyncFunction, "function `fooAsync` cannot be null")
        val asyncResult = asyncFunction.call(fooImpl, obj)
        assertIs<CompletableFuture<out TestObj>>(asyncResult)
        assertSame(obj, asyncResult.join())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFooError() {
        val blockingFunction = FooImpl::class.functions.find { it.name == "fooErrorBlocking" }
        assertNotNull(blockingFunction, "function `fooErrorBlocking` cannot be null")

        val obj = TestObj()

        val blockTargetException = assertFailsWith<InvocationTargetException> {
            blockingFunction.call(fooImpl, obj)
        }.targetException

        assertIs<TestErr>(blockTargetException)

        val asyncFunction = FooImpl::class.functions.find { it.name == "fooErrorAsync" }
        assertNotNull(asyncFunction, "function `fooErrorAsync` cannot be null")

        val asyncResult = asyncFunction.call(fooImpl, obj)
        assertIs<CompletableFuture<String>>(asyncResult)

        val asyncException = assertFailsWith<CompletionException> {
            asyncResult.join()
        }.cause

        assertIs<TestErr>(asyncException)
    }


}