// FIR_DUMP
// DUMP_IR
// SOURCE
// FILE: Main.kt [MainKt#main]
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

open class Bar
class Tar : Bar()

interface Foo<out T : Bar> {
    @JvmBlocking
    @JvmAsync
    suspend fun value(): T

}

class FooImpl : Foo<Tar> {
    override suspend fun value(): Tar {
        return Tar()
    }
}

