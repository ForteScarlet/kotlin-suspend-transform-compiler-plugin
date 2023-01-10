package love.forte.plugin.suspendtrans

data class FunctionInfo(
    val packageName: String,
    val className: String?,
    val functionName: String,
)

data class ClassInfo @JvmOverloads constructor(
    val packageName: String,
    val className: String,
    val local: Boolean = false
)

/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformConfiguration @JvmOverloads constructor(
    var enabled: Boolean = true,
    defaultJvm: Jvm = Jvm(),
    defaultJs: Js = Js()
) {
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
         * 如果 [jvmBlockingFunctionName] 不为null，则会尝试优先解析。
         *
         * @see jvmBlockingFunctionInfo
         */
        @Deprecated("Use jvmBlockingFunctionInfo")
        var jvmBlockingFunctionName: String? = null // JVM_RUN_IN_BLOCKING_FUNCTION_NAME


        /**
         * 格式必须为
         *
         * ```kotlin
         * fun <T> <fun-name>(block: suspend () -> T): T {
         *     // ...
         * }
         * ```
         */
        var jvmBlockingFunctionInfo: FunctionInfo = FunctionInfo(
            JVM_RUN_IN_BLOCKING_FUNCTION_PACKAGE_NAME,
            JVM_RUN_IN_BLOCKING_FUNCTION_CLASS_NAME,
            JVM_RUN_IN_BLOCKING_FUNCTION_FUNCTION_NAME,
        )

        /**
         * 标记为异步函数的标记注解。
         */
        var jvmAsyncMarkAnnotation = MarkAnnotation(TO_JVM_ASYNC_ANNOTATION_NAME)

        /**
         * 如果 [jvmAsyncFunctionName] 不为null，则会尝试优先解析。
         *
         * @see jvmAsyncFunctionInfo
         *
         */
        @Deprecated("Use jvmBlockingFunctionInfo")
        var jvmAsyncFunctionName: String? = null // JVM_RUN_IN_ASYNC_FUNCTION_NAME

        /**
         * 格式必须为:
         *
         * ```kotlin
         * fun <T> <fun-name>(block: suspend () -> T[, scope: CoroutineScope = ...]): CompletableFuture<T> {
         *     // ...
         * }
         * ```
         *
         * 其中，此异步函数可以有第二个参数，此参数格式必须为 [kotlinx.coroutines.CoroutineScope]。
         * 如果存在此参数，当转化函数所处类型自身实现了 [kotlinx.coroutines.CoroutineScope] 时，将会将其自身作为参数填入，类似于：
         *
         * ```kotlin
         * class Bar : CoroutineScope {
         *    @JvmAsync
         *    suspend fun foo(): Foo
         *
         *    @Api4J fun fooAsync(): CompletableFuture<Foo> = runInAsync(block = { foo() }, scope = this)
         * }
         * ```
         * 当前类型不属于 [kotlinx.coroutines.CoroutineScope] 类型时不会使用此参数。
         *
         */
        var jvmAsyncFunctionInfo: FunctionInfo = FunctionInfo(
            JVM_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME,
            JVM_RUN_IN_ASYNC_FUNCTION_CLASS_NAME,
            JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
        )


        /**
         * @see originFunctionIncludeAnnotationInfos
         */
        @Deprecated("Use 'originFunctionIncludeAnnotationInfos'")
        var originFunctionIncludeAnnotations: List<IncludeAnnotation>? = null
//        listOf(
//            IncludeAnnotation("kotlin.jvm.JvmSynthetic")
//        )

        /**
         * 要在被合成的源函数上追加的注解。必须保证不存在参数。
         */
        var originFunctionIncludeAnnotationInfos: List<IncludeAnnotationInfo> = listOf(
            IncludeAnnotationInfo(ClassInfo("kotlin.jvm", "JvmSynthetic"))
        )

        /**
         * @see syntheticBlockingFunctionIncludeAnnotationInfos
         */
        @Deprecated("Use 'syntheticBlockingFunctionIncludeAnnotationInfos'")
        var syntheticBlockingFunctionIncludeAnnotations: List<IncludeAnnotation>? = null
//        listOf(
//            IncludeAnnotation("love.forte.plugin.suspendtrans.annotation.Api4J")
//        )

        /**
         * 要在合成出来的 blocking 函数上追加的额外注解。（不需要指定 `@Generated`）。
         */
        var syntheticBlockingFunctionIncludeAnnotationInfos: List<IncludeAnnotationInfo> = listOf(
            IncludeAnnotationInfo(ClassInfo("love.forte.plugin.suspendtrans.annotation", "Api4J"))
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
        @Deprecated("Use 'syntheticAsyncFunctionIncludeAnnotationInfos'")
        var syntheticAsyncFunctionIncludeAnnotations: List<IncludeAnnotation>? = null
//        listOf(
//            IncludeAnnotation("love.forte.plugin.suspendtrans.annotation.Api4J")
//        )
        /**
         * 要在合成出来的 async 函数上追加的额外注解。（不需要指定 `@Generated`）。
         *
         */
        var syntheticAsyncFunctionIncludeAnnotationInfos: List<IncludeAnnotationInfo> = listOf(
            IncludeAnnotationInfo(ClassInfo("love.forte.plugin.suspendtrans.annotation", "Api4J"))
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
         * @see jsPromiseFunctionInfo
         */
        @Deprecated("Unused")
        var jsPromiseFunctionName: String? = null // JS_RUN_IN_ASYNC_FUNCTION_NAME

        /**
         * 格式必须为
         * ```kotlin
         * fun <T> <fun-name>(block: suspend () -> T): Promise<T> {
         *      // ...
         * }
         * ```
         */
        var jsPromiseFunctionInfo: FunctionInfo = FunctionInfo(
            JS_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME,
            JS_RUN_IN_ASYNC_FUNCTION_CLASS_NAME,
            JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
        )


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

    data class IncludeAnnotationInfo(
        var classInfo: ClassInfo, var repeatable: Boolean = false
    )

    data class ExcludeAnnotation(val name: String)

    class MarkAnnotation(
        var annotationName: String,
        var baseNameProperty: String = "baseName",
        var suffixProperty: String = "suffix",
        var asPropertyProperty: String = "asProperty",
    )
}

