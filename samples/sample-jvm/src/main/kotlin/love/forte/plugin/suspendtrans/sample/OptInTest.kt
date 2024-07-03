package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.suspendCancellableCoroutine
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import kotlin.coroutines.resume
import kotlin.reflect.KClass

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
annotation class OneOptAnno

annotation class Values(val target: KClass<*>)

class OptInTest {
    @OneOptAnno
    private suspend fun run0() = suspendCancellableCoroutine {
        it.resume(1)
    }

    @OptIn(OneOptAnno::class)
    @Values(OptInTest::class)
    @JvmBlocking
    @JvmAsync
    suspend fun run(): Int = run0()
}
