package love.forte.plugin.suspendtrans

import kotlinx.serialization.Serializable

@Serializable
data class FunctionInfo(
    var packageName: String,
    var className: String?,
    var functionName: String,
)

@Serializable
data class ClassInfo @JvmOverloads constructor(
    var packageName: String,
    var className: String,
    var local: Boolean = false,
    var nullable: Boolean = false,
)

@Serializable
enum class TargetPlatform {
    COMMON, JVM, JS, WASM, NATIVE
}

/**
 * 根据某个标记注解，
 * 将一个挂起函数根据一个指定的生成为某种
 *
 */
@Serializable
data class Transformer(
    /** 函数上的某种标记。 */
    val markAnnotation: MarkAnnotation,

    /**
     * 用于转化的函数信息。
     *
     * 这个函数的实际格式必须为
     *
     * ```kotlin
     * fun <T> <fun-name>(block: suspend () -> T[, scope: CoroutineScope = ...]): T {
     *     // ...
     * }
     * ```
     *
     * 其中，此异步函数可以有第二个参数，此参数格式必须为 [kotlinx.coroutines.CoroutineScope]。
     * 如果存在此参数，当转化函数所处类型自身实现了 [kotlinx.coroutines.CoroutineScope] 时，将会将其自身作为参数填入，类似于：
     *
     * ```kotlin
     * class Bar : CoroutineScope {
     *    @Xxx
     *    suspend fun foo(): Foo
     *
     *    @Api4J fun fooXxx(): CompletableFuture<Foo> = transform(block = { foo() }, scope = this)
     * }
     */
    val transformFunctionInfo: FunctionInfo,


    /**
     * 转化后的返回值类型, 为null时代表与原函数一致。
     */
    val transformReturnType: ClassInfo?,

    // TODO TypeGeneric for suspend function return type and transform function return type?

    /**
     * 转化后的返回值类型中，是否存在需要与原本返回值类型一致的泛型。
     */
    val transformReturnTypeGeneric: Boolean,

    /**
     * 函数生成后，需要在原函数上追加的注解信息。
     *
     * 例如追加个 `@kotlin.jvm.JvmSynthetic` 之类的。
     */
    val originFunctionIncludeAnnotations: List<IncludeAnnotation>,

    /**
     * 需要在生成出来的函数上追截的注解信息。（不需要指定 `@Generated`）
     */
    val syntheticFunctionIncludeAnnotations: List<IncludeAnnotation>,

    /**
     * 是否复制源函数上的注解到新的函数上。
     */
    val copyAnnotationsToSyntheticFunction: Boolean,

    /**
     * 复制原函数上注解时需要排除掉的注解。
     */
    val copyAnnotationExcludes: List<ClassInfo>
)

/**
 * 用于标记的注解信息.
 */
@Serializable
data class MarkAnnotation @JvmOverloads constructor(
    /**
     * 注解类信息
     */
    val classInfo: ClassInfo,
    /**
     * 用于标记生成函数需要使用的基础函数名的注解属性名。
     */
    val baseNameProperty: String = "baseName",

    /**
     * 用于标记生成函数需要使用的基础函数名之后的后缀的注解属性名。
     */
    val suffixProperty: String = "suffix",

    /**
     * 用于标记生成函数是否需要转化为 property 类型的注解属性名。
     */
    val asPropertyProperty: String = "asProperty",

    /**
     * 当 [suffixProperty] 不存在时使用的默认后缀
     */
    val defaultSuffix: String = "",

    /**
     * 当 [asPropertyProperty] 不存在时使用的默认值
     */
    val defaultAsProperty: Boolean = false,
)


@Serializable
data class IncludeAnnotation(
    val classInfo: ClassInfo, val repeatable: Boolean = false
)

/**
 *
 * @author ForteScarlet
 */
@Suppress("unused")
@Serializable
open class SuspendTransformConfiguration {
    var enabled: Boolean = true

    var transformers: MutableMap<TargetPlatform, List<Transformer>> = mutableMapOf()

    fun clear() {
        transformers.clear()
    }

    fun useJvmDefault() {
        transformers[TargetPlatform.JVM] = mutableListOf(jvmBlockingTransformer, jvmAsyncTransformer)
    }

    fun useJsDefault() {
        transformers[TargetPlatform.JS] = mutableListOf(jsPromiseTransformer)
    }

    fun useDefault() {
        useJvmDefault()
        useJsDefault()
    }

    fun addTransformers(target: TargetPlatform, vararg transformers: Transformer) {
        this.transformers.compute(target) { _, list ->
            if (list != null) {
                list + transformers
            } else {
                listOf(elements = transformers)
            }
        }
    }

    fun addTransformers(target: TargetPlatform, transformers: Collection<Transformer>) {
        this.transformers.compute(target) { _, list ->
            if (list != null) {
                list + transformers
            } else {
                transformers.toList()
            }
        }
    }

    fun addJvmTransformers(vararg transformers: Transformer) {
        addTransformers(target = TargetPlatform.JVM, transformers = transformers)
    }

    fun addJvmTransformers(transformers: Collection<Transformer>) {
        addTransformers(target = TargetPlatform.JVM, transformers = transformers)
    }

    fun addJsTransformers(vararg transformers: Transformer) {
        addTransformers(target = TargetPlatform.JS, transformers = transformers)
    }

    fun addJsTransformers(transformers: Collection<Transformer>) {
        addTransformers(target = TargetPlatform.JS, transformers = transformers)
    }

    override fun toString(): String {
        return "SuspendTransformConfiguration(enabled=$enabled, transformers=$transformers)"
    }

    companion object {
        //region JVM defaults
        @JvmStatic
        val jvmSyntheticClassInfo = ClassInfo("kotlin.jvm", "JvmSynthetic")

        @JvmStatic
        val jvmApi4JAnnotationClassInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "Api4J")
        //endregion

        //region JVM blocking defaults

        @JvmStatic
        val jvmBlockingMarkAnnotationClassInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "JvmBlocking")

        @JvmStatic
        val jvmBlockingAnnotationInfo = MarkAnnotation(jvmBlockingMarkAnnotationClassInfo, defaultSuffix = "Blocking")

        @JvmStatic
        val jvmBlockingTransformFunction = FunctionInfo(
            JVM_RUN_IN_BLOCKING_FUNCTION_PACKAGE_NAME,
            JVM_RUN_IN_BLOCKING_FUNCTION_CLASS_NAME,
            JVM_RUN_IN_BLOCKING_FUNCTION_FUNCTION_NAME,
        )

        @JvmStatic
        val jvmBlockingTransformer = Transformer(
            markAnnotation = jvmBlockingAnnotationInfo,
            transformFunctionInfo = jvmBlockingTransformFunction,
            transformReturnType = null,
            transformReturnTypeGeneric = false,
            originFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmSyntheticClassInfo)),
            copyAnnotationsToSyntheticFunction = true,
            copyAnnotationExcludes = listOf(jvmSyntheticClassInfo, jvmBlockingAnnotationInfo.classInfo),
            syntheticFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmApi4JAnnotationClassInfo))
        )
        //endregion

        //region JVM async defaults
        @JvmStatic
        val jvmAsyncMarkAnnotationClassInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "JvmAsync")

        @JvmStatic
        val jvmAsyncAnnotationInfo = MarkAnnotation(jvmAsyncMarkAnnotationClassInfo, defaultSuffix = "Async")

        @JvmStatic
        val jvmAsyncTransformFunction = FunctionInfo(
            JVM_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME,
            JVM_RUN_IN_ASYNC_FUNCTION_CLASS_NAME,
            JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
        )

        @JvmStatic
        val jvmAsyncTransformer = Transformer(
            markAnnotation = jvmAsyncAnnotationInfo,
            transformFunctionInfo = jvmAsyncTransformFunction,
            transformReturnType = ClassInfo("java.util.concurrent", "CompletableFuture"),
            transformReturnTypeGeneric = true,
            originFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmSyntheticClassInfo)),
            copyAnnotationsToSyntheticFunction = true,
            copyAnnotationExcludes = listOf(jvmSyntheticClassInfo, jvmAsyncAnnotationInfo.classInfo),
            syntheticFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmApi4JAnnotationClassInfo))
        )
        //endregion

        //region JS defaults
        @JvmStatic
        val jsApi4JsAnnotationInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "Api4Js")

        @JvmStatic
        val jsAsyncMarkAnnotationClassInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "JsPromise")

        @JvmStatic
        val jsAsyncAnnotationInfo = MarkAnnotation(jsAsyncMarkAnnotationClassInfo, defaultSuffix = "Async")

        @JvmStatic
        val jsAsyncTransformFunction = FunctionInfo(
            JS_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME,
            JS_RUN_IN_ASYNC_FUNCTION_CLASS_NAME,
            JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
        )

        @JvmStatic
        val jsPromiseTransformer = Transformer(
            markAnnotation = jsAsyncAnnotationInfo,
            transformFunctionInfo = jsAsyncTransformFunction,
            transformReturnType = ClassInfo("kotlin.js", "Promise"),
            transformReturnTypeGeneric = true,
            originFunctionIncludeAnnotations = listOf(),
            copyAnnotationsToSyntheticFunction = true,
            copyAnnotationExcludes = listOf(jsAsyncAnnotationInfo.classInfo),
            syntheticFunctionIncludeAnnotations = listOf(IncludeAnnotation(jsApi4JsAnnotationInfo))
        )
        //endregion

    }
}

