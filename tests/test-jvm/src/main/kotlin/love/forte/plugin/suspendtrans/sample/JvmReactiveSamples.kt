package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import love.forte.plugin.suspendtrans.annotation.ExperimentalJvmApi
import love.forte.plugin.suspendtrans.annotation.JvmReactive
import kotlin.coroutines.CoroutineContext

open class ReactiveFoo
interface ReactiveMarker
class ReactiveMarkedFoo : ReactiveFoo(), ReactiveMarker

@OptIn(ExperimentalJvmApi::class)
class JvmReactiveSamples {
    @JvmReactive
    suspend fun stringValue(): String = "value"

    @JvmReactive
    suspend fun nullableStringValue(): String? = null

    @JvmReactive
    suspend fun <T : ReactiveFoo?> nullableBoundGenericValue(value: T): T = value

    @JvmReactive
    suspend fun <T> whereGenericValue(value: T): T where T : ReactiveFoo?, T : ReactiveMarker? = value

    @JvmReactive(asProperty = true)
    suspend fun nullablePropertyValue(): String? = null
}

@OptIn(ExperimentalJvmApi::class)
class JvmReactiveScopedSamples(
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default
) : CoroutineScope {
    @JvmReactive
    suspend fun scopedValue(): String = "scoped"
}
