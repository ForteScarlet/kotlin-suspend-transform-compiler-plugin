package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.name.FqName

inline val String.fqn: FqName get() = FqName(this)

const val GENERATED_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.Generated"

val generatedAnnotationName: FqName = GENERATED_ANNOTATION_NAME.fqn

//region JVM
// region jvm blocking


const val TO_JVM_BLOCKING_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.JvmBlocking"

val toJvmBlockingAnnotationName: FqName = TO_JVM_BLOCKING_ANNOTATION_NAME.fqn


const val JVM_RUN_IN_BLOCKING_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runtime.\$runInBlocking\$"

val jvmRunInBlockingFunctionName: FqName = JVM_RUN_IN_BLOCKING_FUNCTION_NAME.fqn
// endregion


// region jvm async
const val TO_JVM_ASYNC_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.JvmAsync"

val toJvmAsyncAnnotationName: FqName = TO_JVM_ASYNC_ANNOTATION_NAME.fqn

const val JVM_RUN_IN_ASYNC_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runtime.\$runInAsync\$"

val jvmRunInAsyncFunctionName: FqName = JVM_RUN_IN_ASYNC_FUNCTION_NAME.fqn

const val COMPLETABLE_FUTURE_CLASS_NAME: String =
    "java.util.concurrent.CompletableFuture"

val completableFutureClassName: FqName = COMPLETABLE_FUTURE_CLASS_NAME.fqn
// endregion
//endregion

// region js promise
const val TO_JS_PROMISE_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.JsPromise"

val toJsPromiseAnnotationName: FqName = TO_JS_PROMISE_ANNOTATION_NAME.fqn

const val JS_RUN_IN_ASYNC_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runtime.\$runInAsync\$"

val jsRunInAsyncFunctionName: FqName = JS_RUN_IN_ASYNC_FUNCTION_NAME.fqn

const val JS_PROMISE_CLASS_NAME: String =
    "kotlin.js.Promise"

val jsPromiseClassName = JS_PROMISE_CLASS_NAME.fqn

// endregion



