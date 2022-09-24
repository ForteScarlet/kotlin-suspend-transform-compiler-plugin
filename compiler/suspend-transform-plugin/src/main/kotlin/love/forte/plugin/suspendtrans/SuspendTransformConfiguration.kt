package love.forte.plugin.suspendtrans


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformConfiguration @JvmOverloads constructor(var enabled: Boolean = true, defaultJvm: Jvm = Jvm(), defaultJs: Js = Js()) {
    var jvm: Jvm = defaultJvm
        private set

    var js: Js = defaultJs
        private set

    fun jvm(block: Jvm.() -> Unit) {
        jvm.block()
    }

    fun js(block: Js.() -> Unit) {
        js.block()
    }

    override fun toString(): String {
        return "SuspendTransformConfiguration(enabled=$enabled, jvm=$jvm, js=$js)"
    }

    /**
     * Jvm platform config
     */
    open class Jvm {

        /**
         * 标记为阻塞函数的标记注解。
         */
        var jvmBlockingMarkAnnotation = MarkAnnotation(TO_JVM_BLOCKING_ANNOTATION_NAME)

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
         * 标记为异步函数的标记注解。
         */
        var jvmAsyncMarkAnnotation = MarkAnnotation(TO_JVM_ASYNC_ANNOTATION_NAME)

        /**
         * 格式必须为
         *
         * ```kotlin
         * fun <T> <fun-name>(block: suspend () -> T): CompletableFuture<T> {
         *     // ...
         * }
         * ```
         */
        var jvmAsyncFunctionName: String = JVM_RUN_IN_ASYNC_FUNCTION_NAME

        /**
         * 要在被合成的源函数上追加的注解。必须保证不存在参数。
         */
        var originFunctionIncludeAnnotations: List<IncludeAnnotation> = listOf(
            IncludeAnnotation("kotlin.jvm.JvmSynthetic")
        )

        /**
         * 要在合成出来的 blocking 函数上追加的额外注解。（不需要指定 `@Generated`）。
         *
         */
        var syntheticBlockingFunctionIncludeAnnotations: List<IncludeAnnotation> = listOf(
            IncludeAnnotation("love.forte.plugin.suspendtrans.annotation.Api4J")
        )

        /**
         * 复制源函数上的注解到新的函数上。
         */
        var copyAnnotationsToSyntheticBlockingFunction: Boolean = true

        /**
         * 如果 [copyAnnotationsToSyntheticBlockingFunction] 为 true，则配置在进行拷贝时需要被排除掉（不进行拷贝）的注解。
         *
         */
        var copyAnnotationsToSyntheticBlockingFunctionExcludes: List<ExcludeAnnotation> = listOf(
            ExcludeAnnotation("kotlin.jvm.JvmSynthetic")
        )

        /**
         * 要在合成出来的 async 函数上追加的额外注解。（不需要指定 `@Generated`）。
         *
         */
        var syntheticAsyncFunctionIncludeAnnotations: List<IncludeAnnotation> = listOf(
            IncludeAnnotation("love.forte.plugin.suspendtrans.annotation.Api4J")
        )


        /**
         * 复制源函数上的注解到新的函数上。
         */
        var copyAnnotationsToSyntheticAsyncFunction: Boolean = true

        /**
         * 如果 [copyAnnotationsToSyntheticAsyncFunction] 为 true，则配置在进行拷贝时需要被排除掉（不进行拷贝）的注解。
         *
         */
        var copyAnnotationsToSyntheticAsyncFunctionExcludes: List<ExcludeAnnotation> = listOf(
            ExcludeAnnotation("kotlin.jvm.JvmSynthetic")
        )

        override fun toString(): String {
            return "Jvm(jvmBlockingMarkAnnotation=$jvmBlockingMarkAnnotation, jvmBlockingFunctionName='$jvmBlockingFunctionName', jvmAsyncMarkAnnotation=$jvmAsyncMarkAnnotation, jvmAsyncFunctionName='$jvmAsyncFunctionName', originFunctionIncludeAnnotations=$originFunctionIncludeAnnotations, syntheticBlockingFunctionIncludeAnnotations=$syntheticBlockingFunctionIncludeAnnotations, copyAnnotationsToSyntheticBlockingFunction=$copyAnnotationsToSyntheticBlockingFunction, copyAnnotationsToSyntheticBlockingFunctionExcludes=$copyAnnotationsToSyntheticBlockingFunctionExcludes, syntheticAsyncFunctionIncludeAnnotations=$syntheticAsyncFunctionIncludeAnnotations, copyAnnotationsToSyntheticAsyncFunction=$copyAnnotationsToSyntheticAsyncFunction, copyAnnotationsToSyntheticAsyncFunctionExcludes=$copyAnnotationsToSyntheticAsyncFunctionExcludes)"
        }


    }

    /**
     * JS platform config
     */
    open class Js {
        /**
         * 格式必须为
         * ```kotlin
         * fun <T> <fun-name>(block: suspend () -> T): Promise<T> {
         *      // ...
         * }
         * ```
         */
        var jsPromiseFunctionName: String = JS_RUN_IN_ASYNC_FUNCTION_NAME


        /**
         * 标记为异步函数的标记注解。
         */
        var jsPromiseMarkAnnotation = MarkAnnotation(TO_JS_PROMISE_ANNOTATION_NAME)


        /**
         * 要在被合成的源函数上追加的注解。必须保证不存在参数。
         */
        var originFunctionIncludeAnnotations: List<IncludeAnnotation> = listOf()

        /**
         * 要在合成出来的 async 函数上追加的额外注解。（不需要指定 `@Generated`）。
         *
         */
        var syntheticAsyncFunctionIncludeAnnotations: List<IncludeAnnotation> = listOf(
            IncludeAnnotation("love.forte.plugin.suspendtrans.annotation.Api4Js")
        )

        /**
         * 复制源函数上的注解到新的函数上。
         */
        var copyAnnotationsToSyntheticAsyncFunction: Boolean = true


        /**
         * 如果 [copyAnnotationsToSyntheticAsyncFunction] 为 true，则配置在进行拷贝时需要被排除掉（不进行拷贝）的注解。
         *
         */
        var copyAnnotationsToSyntheticAsyncFunctionExcludes: List<ExcludeAnnotation> = listOf(
            ExcludeAnnotation("kotlin.jvm.JvmSynthetic")
        )

        override fun toString(): String {
            return "Js(jsPromiseFunctionName='$jsPromiseFunctionName', jsPromiseMarkAnnotation=$jsPromiseMarkAnnotation, originFunctionIncludeAnnotations=$originFunctionIncludeAnnotations, syntheticAsyncFunctionIncludeAnnotations=$syntheticAsyncFunctionIncludeAnnotations, copyAnnotationsToSyntheticAsyncFunction=$copyAnnotationsToSyntheticAsyncFunction, copyAnnotationsToSyntheticAsyncFunctionExcludes=$copyAnnotationsToSyntheticAsyncFunctionExcludes)"
        }

    }


    data class IncludeAnnotation(
        var name: String, var repeatable: Boolean = false
    )

    data class ExcludeAnnotation(val name: String)

    class MarkAnnotation(
        var annotationName: String,
        var baseNameProperty: String = "baseName",
        var suffixProperty: String = "suffix",
        var asPropertyProperty: String = "asProperty",
        // TODO ?
        var functionInheritable: Boolean = false
    )
}

