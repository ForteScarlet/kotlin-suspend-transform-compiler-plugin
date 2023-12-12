import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise

suspend fun main() {
    runInAsync(block = SuspendFun()).await()
}

private val scope = CoroutineScope(EmptyCoroutineContext)

fun <T> runInAsync(block: suspend () -> T): Promise<T> {
    return scope.promise { block() }
}

class SuspendFun : (suspend () -> String) {
    override suspend fun invoke(): String {
        delay(1)
        return "SuspendFun"
    }
}
