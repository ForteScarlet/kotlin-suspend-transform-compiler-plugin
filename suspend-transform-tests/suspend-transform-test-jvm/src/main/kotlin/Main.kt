import love.forte.plugin.suspendtrans.sample.ForteScarlet

suspend fun main() {
    val forte = ForteScarlet()

    println(forte.name())
    println(forte.nameBlocking())
    println(forte.self())
    println(forte.age())
}