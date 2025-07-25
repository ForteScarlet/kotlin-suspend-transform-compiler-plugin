package love.forte.suspendtrans.test.runner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture

private val scope = CoroutineScope(SupervisorJob())

@OptIn(DelicateCoroutinesApi::class)
fun <T> jvmResultToAsync(block: suspend () -> Result<T>): CompletableFuture<T> {
    return scope.future { block().getOrThrow() }
}

@OptIn(DelicateCoroutinesApi::class)
fun <T> jvmResultToBlock(block: suspend () -> Result<T>): T {
    return runBlocking { block().getOrThrow() }
}

@OptIn(DelicateCoroutinesApi::class)
fun <T> jvmResToBlock(block: suspend () -> Res<T>): T {
    return runBlocking { block().value }
}