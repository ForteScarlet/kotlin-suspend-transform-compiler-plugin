@file:JvmName("RunInSuspendJvmKt")

package love.forte.plugin.suspendtrans

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import love.forte.plugin.suspendtrans.annotation.ExperimentalJvmApi
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext


// private val

@ExperimentalJvmApi
public var CoroutineContext4J: CoroutineContext = Dispatchers.IO

@ExperimentalJvmApi
public var CoroutineScope4J: CoroutineScope = CoroutineScope(CoroutineContext4J)

@ExperimentalJvmApi
@Throws(InterruptedException::class)
public fun <T> runInBlocking(
    context: CoroutineContext = CoroutineContext4J,
    block: suspend () -> T,
): T = runBlocking(context) { block() }


private val classLoader: ClassLoader
    get() = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()

private val jdk8Support: Boolean
    get() = runCatching {
        classLoader.loadClass("kotlinx.coroutines.future.FutureKt")
        true
    }.getOrElse { false }

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

private val transformer: FutureTransformer =
    if (jdk8Support) CoroutinesJdk8Transformer
    else SimpleTransformer


@ExperimentalJvmApi
public fun <T> runInAsync(
    scope: CoroutineScope = CoroutineScope4J,
    block: suspend () -> T,
): CompletableFuture<T> {
    return transformer.trans(scope, block)
}




