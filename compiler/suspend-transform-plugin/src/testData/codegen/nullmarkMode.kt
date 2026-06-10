// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

annotation class AsyncNullable
annotation class AsyncNonNull

open class Foo
class Bar : Foo()

class NullmarkMode {
    @AsyncNullable
    suspend fun stringValue(): String = "value"

    @AsyncNullable
    suspend fun <T : Any> genericValue(value: T): T = value

    @AsyncNonNull
    suspend fun nullableStringValue(): String? = "value"

    @AsyncNonNull
    suspend fun <T : Foo?> nullableBoundGenericValue(value: T): T = value
}
