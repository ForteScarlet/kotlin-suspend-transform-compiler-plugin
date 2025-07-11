@file:JvmName("RunInSuspendJvmKt")

package love.forte.plugin.suspendtrans.runtime

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext


// private val

@Suppress("ObjectPropertyName", "unused")
private val `$CoroutineContext4J$`: CoroutineContext = Dispatchers.IO

@Suppress("ObjectPropertyName", "unused")
private val `$CoroutineScope4J$`: CoroutineScope = CoroutineScope(`$CoroutineContext4J$`)

@Suppress("FunctionName")
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@Throws(InterruptedException::class)
public fun <T> `$runInBlocking$`(block: suspend () -> T): T = runBlocking(`$CoroutineContext4J$`) {
    block()
}


private val classLoader: ClassLoader
    get() = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()

// 现在是不是最低也是JDK8了？
// 也许这个判断已经不需要了？
private val jdk8Support: Boolean by lazy {
    runCatching {
        classLoader.loadClass("kotlinx.coroutines.future.FutureKt")
        true
    }.getOrElse { false }
}

private interface FutureTransformer {
    fun <T> trans(scope: CoroutineScope, block: suspend () -> T): CompletableFuture<T>
}

private object CoroutinesJdk8Transformer : FutureTransformer {
    override fun <T> trans(scope: CoroutineScope, block: suspend () -> T): CompletableFuture<T> {
        return scope.future { block() }
    }
}

private object SimpleTransformer : FutureTransformer {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T> trans(scope: CoroutineScope, block: suspend () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        val deferred = scope.async { block() }
        future.whenComplete { _, exception ->
            deferred.cancel(exception?.let {
                it as? CancellationException ?: CancellationException(
                    "CompletableFuture was completed exceptionally",
                    it
                )
            })
        }
        deferred.invokeOnCompletion {
            try {
                future.complete(deferred.getCompleted())
            } catch (t: Throwable) {
                future.completeExceptionally(t)
            }
        }
        return future
    }
}

private val transformer: FutureTransformer by lazy {
    if (jdk8Support) CoroutinesJdk8Transformer
    else SimpleTransformer
}


@OptIn(DelicateCoroutinesApi::class)
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@Suppress("FunctionName")
public fun <T> `$runInAsync$`(
    block: suspend () -> T,
    scope: CoroutineScope? = null
): CompletableFuture<T> =
    // 比起在内部构建一个使用 Dispatchers.IO、并且永不被关闭的 CoroutineScope，
    // 为何不直接使用 GlobalScope？
    // 作为作用域：它用不被关闭、无法被关闭
    // 作为异步调度器：它没必要使用IO
    // 而对于更加复杂的场景，也许需要考虑完全定制化，
    // 而不是使用当前这个简单的runtime包内的实现
    transformer.trans(scope ?: GlobalScope, block)





@OptIn(DelicateCoroutinesApi::class)
public fun <T> jvmResultToBlock(block: suspend () -> Result<T>): T {
    return runBlocking { block().getOrThrow() }
}