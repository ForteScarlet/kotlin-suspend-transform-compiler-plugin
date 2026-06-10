// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]

annotation class AsyncNullable
annotation class AsyncNonNull

open class Foo
open class Bar : Foo()
interface Marker
class MarkedBar : Bar(), Marker

class NullmarkModeWhere {
    @AsyncNullable
    suspend fun <T> whereGenericValue(value: T): T where T : Foo, T : Marker = value

    @AsyncNonNull
    suspend fun <T> nullableWhereGenericValue(value: T): T where T : Foo?, T : Marker? = value

    @AsyncNonNull
    suspend fun <T> nonNullToNonNullWhereGenericValue(value: T): T where T : Foo, T : Marker? = value
}
