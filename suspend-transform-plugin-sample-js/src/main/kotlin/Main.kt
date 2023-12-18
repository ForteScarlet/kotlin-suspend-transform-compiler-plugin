import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise

suspend fun main() {
//    runInAsync(block = SuspendFun()).await()
    println(ForteScarlet().stringToInt("1"))
    println(ForteScarlet().stringToIntAsync("1"))
    println(ForteScarlet().stringToIntAsync("1").await())
}
