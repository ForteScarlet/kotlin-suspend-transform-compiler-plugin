import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise

suspend fun main() {
//    runInAsync(block = SuspendFun()).await()
    println(ForteScarlet().stringToInt("1"))
    println(ForteScarlet().stringToIntAsync("1"))
}

private val scope = CoroutineScope(EmptyCoroutineContext)

fun <T> runInAsync(block: suspend () -> T): Promise<T> {
    val b1 = block::invoke
    return scope.promise { b1() }
}

class SuspendFun : (suspend () -> String) {
    override suspend fun invoke(): String {
        delay(1)
        return "SuspendFun"
    }
}
