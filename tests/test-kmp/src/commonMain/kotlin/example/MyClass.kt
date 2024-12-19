package example

import love.forte.plugin.suspendtrans.annotation.JvmBlocking

expect class MoneyValue

class MyClass {
    @JvmBlocking
    suspend fun errorReproduction(amount: MoneyValue) = println(amount)
}
