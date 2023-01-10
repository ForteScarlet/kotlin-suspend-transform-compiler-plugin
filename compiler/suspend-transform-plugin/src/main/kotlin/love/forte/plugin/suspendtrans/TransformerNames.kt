package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

inline val String.fqn: FqName get() = FqName(this)

inline val String.callableId: CallableId get() {
    val splitList = this.split('.')

    val callableName = splitList.last()
    val className = splitList[splitList.lastIndex - 1].takeIf { it.first().isUpperCase() }
    val packageName = splitList.subList(0, splitList.lastIndex - (if (className == null) 1 else 2)).joinToString(".")

    return CallableId(
        packageName = packageName.fqn,
        className = className?.fqn,
        callableName = Name.identifier(callableName),
    )
}

const val GENERATED_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.Generated"

val generatedAnnotationName: FqName = GENERATED_ANNOTATION_NAME.fqn

val generatedAnnotationClassId = ClassId.topLevel(generatedAnnotationName)

const val GENERATED_BY_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.GeneratedBy"

val generatedByAnnotationName: FqName = GENERATED_BY_ANNOTATION_NAME.fqn

//region JVM
// region jvm blocking
const val TO_JVM_BLOCKING_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.JvmBlocking"

val toJvmBlockingAnnotationName: FqName = TO_JVM_BLOCKING_ANNOTATION_NAME.fqn


//region jvm run in blocking function
@Deprecated("Unused")
const val JVM_RUN_IN_BLOCKING_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runtime.\$runInBlocking\$"

const val JVM_RUN_IN_BLOCKING_FUNCTION_PACKAGE_NAME: String = "love.forte.plugin.suspendtrans.runtime"

@JvmField
val JVM_RUN_IN_BLOCKING_FUNCTION_CLASS_NAME: String? = null

const val JVM_RUN_IN_BLOCKING_FUNCTION_FUNCTION_NAME: String = "\$runInBlocking\$"
//endregion


// endregion


// region jvm async
const val TO_JVM_ASYNC_ANNOTATION_NAME: String =
    "love.forte.plugin.suspendtrans.annotation.JvmAsync"

val toJvmAsyncAnnotationName: FqName = TO_JVM_ASYNC_ANNOTATION_NAME.fqn

@Deprecated("Unused")
const val JVM_RUN_IN_ASYNC_FUNCTION_NAME: String =
    "love.forte.plugin.suspendtrans.runtime.\$runInAsync\$"

const val JVM_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME: String = "love.forte.plugin.suspendtrans.runtime"

@JvmField
val JVM_RUN_IN_ASYNC_FUNCTION_CLASS_NAME: String? = null

const val JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME: String = "\$runInAsync\$"


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



const val JS_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME: String = "love.forte.plugin.suspendtrans.runtime"

@JvmField
val JS_RUN_IN_ASYNC_FUNCTION_CLASS_NAME: String? = null

const val JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME: String = "\$runInAsync\$"


const val JS_PROMISE_CLASS_NAME: String =
    "kotlin.js.Promise"

val jsPromiseClassName = JS_PROMISE_CLASS_NAME.fqn

// endregion



