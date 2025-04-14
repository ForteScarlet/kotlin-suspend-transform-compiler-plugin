@file:Suppress("DEPRECATION")

package love.forte.plugin.suspendtrans

import kotlinx.serialization.Serializable


private const val JVM_RUN_IN_BLOCKING_FUNCTION_PACKAGE_NAME: String = "love.forte.plugin.suspendtrans.runtime"
private val JVM_RUN_IN_BLOCKING_FUNCTION_CLASS_NAME: String? = null
private const val JVM_RUN_IN_BLOCKING_FUNCTION_FUNCTION_NAME: String = "\$runInBlocking\$"

private const val JVM_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME: String = "love.forte.plugin.suspendtrans.runtime"
private val JVM_RUN_IN_ASYNC_FUNCTION_CLASS_NAME: String? = null
private const val JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME: String = "\$runInAsync\$"

private const val JS_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME: String = "love.forte.plugin.suspendtrans.runtime"
private val JS_RUN_IN_ASYNC_FUNCTION_CLASS_NAME: String? = null
private const val JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME: String = "\$runInAsync\$"


@Serializable
@Deprecated(
    "Use new `love.forte.plugin.suspendtrans.configuration.FunctionInfo` instead.",
    ReplaceWith("FunctionInfo", "love.forte.plugin.suspendtrans.configuration.FunctionInfo")
)
data class FunctionInfo(
    var packageName: String,
    @Deprecated("Top-Level function supported only")
    var className: String? = null,
    var functionName: String,
)

@Serializable
@Deprecated(
    "Use new `love.forte.plugin.suspendtrans.configuration.ClassInfo` instead.",
    ReplaceWith("ClassInfo", "love.forte.plugin.suspendtrans.configuration.ClassInfo")
)
data class ClassInfo @JvmOverloads constructor(
    var packageName: String,
    var className: String,
    var local: Boolean = false,
    var nullable: Boolean = false,
)

@Serializable
@Deprecated(
    "Use new `love.forte.plugin.suspendtrans.configuration.TargetPlatform` instead.",
    ReplaceWith("TargetPlatform", "love.forte.plugin.suspendtrans.configuration.TargetPlatform")
)
enum class TargetPlatform {
    COMMON, JVM, JS, WASM, NATIVE
}

/**
 * 根据某个标记注解，
 * 将一个挂起函数根据一个指定的生成为某种
 *
 */
@Serializable
@Deprecated(
    "Use new `love.forte.plugin.suspendtrans.configuration.Transformer` instead.",
    replaceWith = ReplaceWith(
        "Transformer",
        "love.forte.plugin.suspendtrans.configuration.Transformer"
    )
)
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
     * 需要在生成出来的函数上追加的注解信息。（不需要指定 `@Generated`）
     */
    val syntheticFunctionIncludeAnnotations: List<IncludeAnnotation>,

    /**
     * 是否复制源函数上的注解到新的函数上。
     * 如果生成的是属性类型，则表示是否复制到 `getter` 上。
     */
    val copyAnnotationsToSyntheticFunction: Boolean,

    /**
     * 复制原函数上注解时需要排除掉的注解。
     */
    val copyAnnotationExcludes: List<ClassInfo>
) {
    /**
     * 如果是生成属性的话，是否复制源函数上的注解到新的属性上
     *
     * @since 0.9.0
     */
    var copyAnnotationsToSyntheticProperty: Boolean = false
}

/**
 * 用于标记的注解信息.
 */
@Serializable
@Deprecated(
    "Use new `love.forte.plugin.suspendtrans.configuration.MarkAnnotation` instead.",
    ReplaceWith("MarkAnnotation", "love.forte.plugin.suspendtrans.configuration.MarkAnnotation")
)
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
@Deprecated(
    "Use new `love.forte.plugin.suspendtrans.configuration.IncludeAnnotation` instead.",
    ReplaceWith("IncludeAnnotation", "love.forte.plugin.suspendtrans.configuration.IncludeAnnotation")
)
data class IncludeAnnotation(
    val classInfo: ClassInfo, val repeatable: Boolean = false
) {
    /**
     * 如果是追加，是否追加到property上
     *
     * @since 0.9.0
     */
    var includeProperty: Boolean = false
}

const val USE_NEW_EXTENSION = "Please use the `love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration`" +
        "(`suspendTransformPlugin { ... }`) instead."

/**
 *
 * @author ForteScarlet
 */
@Suppress("unused")
@Serializable
@Deprecated(
    message = USE_NEW_EXTENSION,
    replaceWith = ReplaceWith(
        "SuspendTransformConfiguration",
        "love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration"
    )
)
open class SuspendTransformConfiguration {
    @Deprecated(USE_NEW_EXTENSION)
    open var enabled: Boolean = true

    @Deprecated(USE_NEW_EXTENSION)
    open var transformers: MutableMap<TargetPlatform, List<Transformer>> = mutableMapOf()

    /**
     * 在 K2 中，用于使 IR 的合成函数可以定位到 FIR 中原始函数的辅助注解。
     *
     * 昙花一现，在 `*-0.11.0` 之后不再需要此类过渡用注解。
     *
     * @since *-0.10.0
     */
    @Deprecated("Unused after *-0.11.0")
    open var targetMarker: ClassInfo? = targetMarkerClassInfo

    @Deprecated(USE_NEW_EXTENSION)
    open fun clear() {
        transformers.clear()
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun useJvmDefault() {
        transformers[TargetPlatform.JVM] = mutableListOf(jvmBlockingTransformer, jvmAsyncTransformer)
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun useJsDefault() {
        transformers[TargetPlatform.JS] = mutableListOf(jsPromiseTransformer)
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun useDefault() {
        useJvmDefault()
        useJsDefault()
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun addTransformers(target: TargetPlatform, vararg transformers: Transformer) {
        this.transformers.compute(target) { _, list ->
            if (list != null) {
                list + transformers
            } else {
                listOf(elements = transformers)
            }
        }
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun addTransformers(target: TargetPlatform, transformers: Collection<Transformer>) {
        this.transformers.compute(target) { _, list ->
            if (list != null) {
                list + transformers
            } else {
                transformers.toList()
            }
        }
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun addJvmTransformers(vararg transformers: Transformer) {
        addTransformers(target = TargetPlatform.JVM, transformers = transformers)
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun addJvmTransformers(transformers: Collection<Transformer>) {
        addTransformers(target = TargetPlatform.JVM, transformers = transformers)
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun addJsTransformers(vararg transformers: Transformer) {
        addTransformers(target = TargetPlatform.JS, transformers = transformers)
    }

    @Deprecated(USE_NEW_EXTENSION)
    open fun addJsTransformers(transformers: Collection<Transformer>) {
        addTransformers(target = TargetPlatform.JS, transformers = transformers)
    }

    override fun toString(): String {
        return "SuspendTransformConfiguration(enabled=$enabled, transformers=$transformers)"
    }

    @Deprecated(USE_NEW_EXTENSION)
    companion object {
        val targetMarkerClassInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "TargetMarker")

        //region JVM defaults
        @JvmStatic
        val jvmSyntheticClassInfo = ClassInfo("kotlin.jvm", "JvmSynthetic")

        @JvmStatic
        val kotlinOptInClassInfo = ClassInfo("kotlin", "OptIn")

        @JvmStatic
        val jvmApi4JAnnotationClassInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "Api4J")


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
        val jvmAsyncMarkAnnotationClassInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "JvmAsync")

        @JvmStatic
        val jvmAsyncAnnotationInfo = MarkAnnotation(jvmAsyncMarkAnnotationClassInfo, defaultSuffix = "Async")

        @JvmStatic
        val jvmAsyncTransformFunction = FunctionInfo(
            JVM_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME,
            JVM_RUN_IN_ASYNC_FUNCTION_CLASS_NAME,
            JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
        )
        //endregion

        //region JVM blocking defaults
        @JvmStatic
        val jvmBlockingTransformer = Transformer(
            markAnnotation = jvmBlockingAnnotationInfo,
            transformFunctionInfo = jvmBlockingTransformFunction,
            transformReturnType = null,
            transformReturnTypeGeneric = false,
            originFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmSyntheticClassInfo)),
            copyAnnotationsToSyntheticFunction = true,
            copyAnnotationExcludes = listOf(
                jvmSyntheticClassInfo,
                jvmBlockingMarkAnnotationClassInfo,
                jvmAsyncMarkAnnotationClassInfo,
                kotlinOptInClassInfo,
            ),
            syntheticFunctionIncludeAnnotations = listOf(
                IncludeAnnotation(jvmApi4JAnnotationClassInfo)
                    .apply { includeProperty = true }
            )
        )
        //endregion

        //region JVM async defaults

        @JvmStatic
        val jvmAsyncTransformer = Transformer(
            markAnnotation = jvmAsyncAnnotationInfo,
            transformFunctionInfo = jvmAsyncTransformFunction,
            transformReturnType = ClassInfo("java.util.concurrent", "CompletableFuture"),
            transformReturnTypeGeneric = true,
            originFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmSyntheticClassInfo)),
            copyAnnotationsToSyntheticFunction = true,
            copyAnnotationExcludes = listOf(
                jvmSyntheticClassInfo,
                jvmBlockingMarkAnnotationClassInfo,
                jvmAsyncMarkAnnotationClassInfo,
                kotlinOptInClassInfo,
            ),
            syntheticFunctionIncludeAnnotations = listOf(
                IncludeAnnotation(jvmApi4JAnnotationClassInfo).apply {
                    includeProperty = true
                }
            )
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
            love.forte.plugin.suspendtrans.JS_RUN_IN_ASYNC_FUNCTION_PACKAGE_NAME,
            love.forte.plugin.suspendtrans.JS_RUN_IN_ASYNC_FUNCTION_CLASS_NAME,
            love.forte.plugin.suspendtrans.JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
        )

        @JvmStatic
        val jsPromiseTransformer = Transformer(
            markAnnotation = jsAsyncAnnotationInfo,
            transformFunctionInfo = jsAsyncTransformFunction,
            transformReturnType = ClassInfo("kotlin.js", "Promise"),
            transformReturnTypeGeneric = true,
            originFunctionIncludeAnnotations = listOf(),
            copyAnnotationsToSyntheticFunction = true,
            copyAnnotationExcludes = listOf(
                jsAsyncMarkAnnotationClassInfo,
                kotlinOptInClassInfo,
            ),
            syntheticFunctionIncludeAnnotations = listOf(
                IncludeAnnotation(jsApi4JsAnnotationInfo).apply {
                    includeProperty = true
                }
            )
        )
        //endregion

    }
}

