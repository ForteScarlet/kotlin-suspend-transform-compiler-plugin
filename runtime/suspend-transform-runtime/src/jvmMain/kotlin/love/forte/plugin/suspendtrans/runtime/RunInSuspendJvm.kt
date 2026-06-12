/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:JvmName("RunInSuspendJvmKt")

package love.forte.plugin.suspendtrans.runtime

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import kotlinx.coroutines.reactive.publish
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("ObjectPropertyName")
private val `$CoroutineContext4J$`: CoroutineContext = Dispatchers.IO

/**
 * Runs [block] to completion for generated JVM blocking bridges.
 *
 * This bridge blocks the calling thread and runs the suspend body with
 * [Dispatchers.IO]. The IO dispatcher is a conservative default for Java
 * interop and potentially blocking suspend work, but the caller is still
 * blocked until completion.
 *
 * Do not call generated blocking bridges from coroutines, UI/event-loop
 * threads, or other thread-limited execution paths. Use a custom transform
 * runtime when a specific dispatcher, transaction context, MDC, or thread
 * affinity is required.
 */
@Suppress("FunctionName")
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@Throws(InterruptedException::class)
public fun <T> `$runInBlocking$`(block: suspend () -> T): T = runBlocking(`$CoroutineContext4J$`) {
    block()
}


private val classLoader: ClassLoader
    get() = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()

// The minimum JVM target may already make this check redundant.
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

/**
 * Runs [block] as a [CompletableFuture] for generated JVM async bridges.
 *
 * If [scope] is provided, the coroutine is launched in that scope. Otherwise,
 * this bridge uses [GlobalScope]. Cancelling the returned future cancels the
 * coroutine; dropping the future without cancellation does not.
 */
@OptIn(DelicateCoroutinesApi::class)
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@Suppress("FunctionName")
public fun <T> `$runInAsync$`(
    block: suspend () -> T,
    scope: CoroutineScope? = null
): CompletableFuture<T> {
    // This simple runtime bridge uses GlobalScope instead of keeping an internal
    // Dispatchers.IO scope that would also live for the whole process lifetime.
    // More advanced lifecycle needs should use a custom transform runtime.
    return transformer.trans(scope ?: GlobalScope, block)
}

/**
 * Runs [block] as a cold Reactive Streams [Publisher].
 *
 * The returned publisher emits one non-null value when [block] completes with a
 * non-null result, completes empty when [block] returns `null`, and propagates
 * failures as publisher errors.
 *
 * The [scope] parameter is kept for generated bridge compatibility and for a
 * possible future runtime strategy, but it is intentionally ignored by this
 * default bridge. It is not used as a coroutine parent or dispatcher source.
 *
 * Reactive Streams lifecycle is controlled by `Subscription.cancel()`, and
 * dispatcher or lifecycle requirements should be expressed inside [block] or by
 * a custom transform runtime.
 */
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@Suppress("FunctionName", "UNUSED_PARAMETER")
public fun <T> `$runInReactive$`(
    block: suspend () -> T,
    scope: CoroutineScope? = null
): Publisher<T & Any> {
    // Reactive APIs should keep scheduling and lifecycle decisions at the
    // subscription chain or in the suspend block, so this default bridge does
    // not attach the publisher coroutine to an owner CoroutineScope.
    return publish(EmptyCoroutineContext) {
        block()?.let { send(it) }
    }
}
