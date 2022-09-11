package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.name.FqName

public const val GENERATED_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.Generated"

public val generatedAnnotationName: FqName =
    FqName(GENERATED_ANNOTATION_NAME)

// region jvm blocking
public const val TO_JVM_BLOCKING_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.Suspend2JvmBlocking"

public val toJvmBlockingAnnotationName: FqName =
    FqName(TO_JVM_BLOCKING_ANNOTATION_NAME)


public const val JVM_RUN_IN_BLOCKING_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runInBlocking"

public val jvmRunInBlockingFunctionName: FqName =
    FqName(JVM_RUN_IN_BLOCKING_FUNCTION_NAME)
// endregion


// region jvm async
public const val TO_JVM_ASYNC_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.Suspend2JvmAsync"

public val toJvmAsyncAnnotationName: FqName =
    FqName(TO_JVM_ASYNC_ANNOTATION_NAME)

public const val JVM_RUN_IN_ASYNC_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runInAsync"

public val jvmRunInAsyncFunctionName: FqName =
    FqName(JVM_RUN_IN_ASYNC_FUNCTION_NAME)

public const val COMPLETABLE_FUTURE_CLASS_NAME: String =
    "java.util.concurrent.CompletableFuture"

public val completableFutureClassName: FqName =
    FqName(COMPLETABLE_FUTURE_CLASS_NAME)

// endregion

// region js promise
public const val TO_JS_PROMISE_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.Suspend2JsPromise"

public val toJsPromiseAnnotationName: FqName =
    FqName(TO_JS_PROMISE_ANNOTATION_NAME)

public const val JS_RUN_IN_ASYNC_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runInAsync"

public val jsRunInAsyncFunctionName: FqName =
    FqName(JS_RUN_IN_ASYNC_FUNCTION_NAME)

// endregion



