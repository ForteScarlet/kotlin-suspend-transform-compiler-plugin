package love.forte.plugin.suspendtrans.sample

import kotlinx.coroutines.delay

annotation class GenericAnno<T>()


@GenericAnno<String?>()
suspend fun run(): Result<String> {
    delay(1)
    return Result.success("ok")
}