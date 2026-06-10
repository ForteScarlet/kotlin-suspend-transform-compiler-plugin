package love.forte.plugin.suspendtrans.sample

annotation class JvmNullableAsync
annotation class JvmNonNullAsync

open class NullmarkFoo
open class NullmarkBar : NullmarkFoo()
interface NullmarkMarker
class NullmarkMarkedBar : NullmarkBar(), NullmarkMarker

class NullmarkModeSamples {
    @JvmNullableAsync
    suspend fun stringValue(): String = "value"

    @JvmNullableAsync
    suspend fun <T : Any> genericValue(value: T): T = value

    @JvmNonNullAsync
    suspend fun nullableStringValue(): String? = "value"

    @JvmNonNullAsync
    suspend fun <T : NullmarkFoo?> nullableBoundGenericValue(value: T): T = value

    @JvmNullableAsync
    suspend fun <T> whereGenericValue(value: T): T where T : NullmarkFoo, T : NullmarkMarker = value

    @JvmNonNullAsync
    suspend fun <T> nullableWhereGenericValue(value: T): T where T : NullmarkFoo?, T : NullmarkMarker? = value
}
