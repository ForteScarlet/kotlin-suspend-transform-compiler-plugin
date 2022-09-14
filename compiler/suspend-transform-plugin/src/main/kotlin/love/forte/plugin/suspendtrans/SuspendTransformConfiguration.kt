package love.forte.plugin.suspendtrans


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformConfiguration {


    /**
     * Jvm platform config
     */
    open class Jvm {
        /**
         * 格式必须为
         *
         * ```kotlin
         * fun <T> <fun-name>(block: suspend () -> T): T {
         *     // ...
         * }
         * ```
         */
        var jvmBlockingFunctionName: String = JVM_RUN_IN_BLOCKING_FUNCTION_NAME

        /**
         * 格式必须为
         *
         * ```kotlin
         * fun <T> <fun-name>(block: suspend () -> T): CompletableFuture<T> {
         *     // ...
         * }
         * ```
         */
        var jvmAsyncFunctionName: String = JS_RUN_IN_ASYNC_FUNCTION_NAME

        /**
         * 要在被合成的源函数上追加的注解。必须保证不存在参数。
         */
        var originFunctionIncludeAnnotations: List<IncludeAnnotation> = listOf(
            IncludeAnnotation( "kotlin.jvm.JvmSynthetic"),
            IncludeAnnotation( "kotlin.jvm.JvmSynthetic")
        )


    }


    open class Js


    data class IncludeAnnotation(
        val name: String,
        val repeatable: Boolean = false
    ) {
        companion object {
            @JvmField
            val jvmSynthetic = IncludeAnnotation( "kotlin.jvm.JvmSynthetic")
        }
    }
}

