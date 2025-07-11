package love.forte.plugin.suspendtrans.sample.returntypeoverride

import kotlinx.coroutines.delay
import love.forte.suspendtrans.test.runner.JvmResBlock
import love.forte.suspendtrans.test.runner.JvmResultAsync
import love.forte.suspendtrans.test.runner.JvmResultBlock
import love.forte.suspendtrans.test.runner.Res


interface Foo<R> {
    @JvmResultBlock<String>
    @JvmResultAsync<String>
    suspend fun hello(): Result<String> {
        delay(1)
        return Result.success("Hello, ")
    }

    // @JvmResultBlock<T>
    @JvmResultAsync<T>
    suspend fun <T> foo(value: T): Result<T> {
        delay(1)
        return Result.success(value)
    }
    // TODO
    //  Caused by: java.lang.NullPointerException
    // 	at org.jetbrains.kotlin.ir.util.IrUtilsKt.getTypeSubstitutionMap(IrUtils.kt:580)
    // 	at org.jetbrains.kotlin.ir.util.IrUtilsKt.getTypeSubstitutionMap(IrUtils.kt:588)
    // 	at org.jetbrains.kotlin.backend.jvm.lower.JvmInlineClassLowering.visitFunctionAccess(JvmInlineClassLowering.kt:259)
    // 	at org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid.visitCall(IrElementTransformerVoid.kt:296)
    // 	at org.jetbrains.kotlin.backend.jvm.lower.JvmInlineClassLowering.visitCall(JvmInlineClassLowering.kt:405)
    // 	at org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid.visitCall(IrElementTransformerVoid.kt:299)
    // 	at org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid.visitCall(IrElementTransformerVoid.kt:19)
    // 	at org.jetbrains.kotlin.ir.expressions.IrCall.accept(IrCall.kt:24)

    @JvmResBlock<T>
    // @JvmResultAsync<T>
    suspend fun <T> foo1(value: T): Res<T> {
        delay(1)
        return Res(value)
    }

}