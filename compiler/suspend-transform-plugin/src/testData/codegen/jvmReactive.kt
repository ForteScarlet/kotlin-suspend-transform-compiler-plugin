import love.forte.plugin.suspendtrans.annotation.ExperimentalJvmApi
import love.forte.plugin.suspendtrans.annotation.JvmReactive

open class Foo
interface Marker

@OptIn(ExperimentalJvmApi::class)
class JvmReactiveCodegen {
    @JvmReactive
    suspend fun stringValue(): String = ""

    @JvmReactive
    suspend fun nullableStringValue(): String? = null

    @JvmReactive
    suspend fun <T : Foo?> nullableBoundGenericValue(value: T): T = value

    @JvmReactive
    suspend fun <T> whereGenericValue(value: T): T where T : Foo?, T : Marker? = value

    @JvmReactive(asProperty = true)
    suspend fun nullablePropertyValue(): String? = null
}
