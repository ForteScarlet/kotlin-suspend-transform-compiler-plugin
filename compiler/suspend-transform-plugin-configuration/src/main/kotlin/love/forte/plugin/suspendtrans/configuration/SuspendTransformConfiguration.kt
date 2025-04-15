package love.forte.plugin.suspendtrans.configuration

import kotlinx.serialization.Serializable

// NOTE:
//   可序列号的配置信息均使用 `Protobuf` 进行序列化
//   虽然序列化行为是内部的，但是还是应该尽可能避免出现字段的顺序错乱或删改。

@RequiresOptIn(
    "This is an internal suspend transform config api. " +
            "It may be changed in the future without notice.", RequiresOptIn.Level.ERROR
)
annotation class InternalSuspendTransformConstructorApi

@Serializable
class FunctionInfo @InternalSuspendTransformConstructorApi constructor(
    val packageName: String,
    val functionName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FunctionInfo) return false

        if (packageName != other.packageName) return false
        if (functionName != other.functionName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + functionName.hashCode()
        return result
    }

    override fun toString(): String {
        return "FunctionInfo(functionName='$functionName', packageName='$packageName')"
    }
}

@Serializable
class ClassInfo @InternalSuspendTransformConstructorApi constructor(
    val packageName: String,
    val className: String,
    val local: Boolean = false,
    val nullable: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassInfo) return false

        if (local != other.local) return false
        if (nullable != other.nullable) return false
        if (packageName != other.packageName) return false
        if (className != other.className) return false

        return true
    }

    override fun hashCode(): Int {
        var result = local.hashCode()
        result = 31 * result + nullable.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + className.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClassInfo(className='$className', packageName='$packageName', local=$local, nullable=$nullable)"
    }
}

@Serializable
enum class TargetPlatform {
    COMMON, JVM, JS, WASM, NATIVE
}

/**
 * 用于标记的注解信息.
 */
@Serializable
class MarkAnnotation @InternalSuspendTransformConstructorApi constructor(
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarkAnnotation) return false

        if (defaultAsProperty != other.defaultAsProperty) return false
        if (classInfo != other.classInfo) return false
        if (baseNameProperty != other.baseNameProperty) return false
        if (suffixProperty != other.suffixProperty) return false
        if (asPropertyProperty != other.asPropertyProperty) return false
        if (defaultSuffix != other.defaultSuffix) return false

        return true
    }

    override fun hashCode(): Int {
        var result = defaultAsProperty.hashCode()
        result = 31 * result + classInfo.hashCode()
        result = 31 * result + baseNameProperty.hashCode()
        result = 31 * result + suffixProperty.hashCode()
        result = 31 * result + asPropertyProperty.hashCode()
        result = 31 * result + defaultSuffix.hashCode()
        return result
    }

    override fun toString(): String {
        return "MarkAnnotation(asPropertyProperty='$asPropertyProperty', classInfo=$classInfo, baseNameProperty='$baseNameProperty', suffixProperty='$suffixProperty', defaultSuffix='$defaultSuffix', defaultAsProperty=$defaultAsProperty)"
    }
}

@Serializable
class IncludeAnnotation @InternalSuspendTransformConstructorApi constructor(
    val classInfo: ClassInfo,
    val repeatable: Boolean = false,
    /**
     * 是否追加到property上
     *
     * @since 0.9.0
     */
    val includeProperty: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IncludeAnnotation) return false

        if (repeatable != other.repeatable) return false
        if (includeProperty != other.includeProperty) return false
        if (classInfo != other.classInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = repeatable.hashCode()
        result = 31 * result + includeProperty.hashCode()
        result = 31 * result + classInfo.hashCode()
        return result
    }

    override fun toString(): String {
        return "IncludeAnnotation(classInfo=$classInfo, repeatable=$repeatable, includeProperty=$includeProperty)"
    }
}

@Serializable
class Transformer @InternalSuspendTransformConstructorApi constructor(
    /**
     * 函数上的某种标记。
     */
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
    val copyAnnotationExcludes: List<ClassInfo>,

    /**
     * 如果是生成属性的话，是否复制源函数上的注解到新的属性上
     *
     * @since 0.9.0
     */
    val copyAnnotationsToSyntheticProperty: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transformer) return false

        if (transformReturnTypeGeneric != other.transformReturnTypeGeneric) return false
        if (copyAnnotationsToSyntheticFunction != other.copyAnnotationsToSyntheticFunction) return false
        if (copyAnnotationsToSyntheticProperty != other.copyAnnotationsToSyntheticProperty) return false
        if (markAnnotation != other.markAnnotation) return false
        if (transformFunctionInfo != other.transformFunctionInfo) return false
        if (transformReturnType != other.transformReturnType) return false
        if (originFunctionIncludeAnnotations != other.originFunctionIncludeAnnotations) return false
        if (syntheticFunctionIncludeAnnotations != other.syntheticFunctionIncludeAnnotations) return false
        if (copyAnnotationExcludes != other.copyAnnotationExcludes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transformReturnTypeGeneric.hashCode()
        result = 31 * result + copyAnnotationsToSyntheticFunction.hashCode()
        result = 31 * result + copyAnnotationsToSyntheticProperty.hashCode()
        result = 31 * result + markAnnotation.hashCode()
        result = 31 * result + transformFunctionInfo.hashCode()
        result = 31 * result + (transformReturnType?.hashCode() ?: 0)
        result = 31 * result + originFunctionIncludeAnnotations.hashCode()
        result = 31 * result + syntheticFunctionIncludeAnnotations.hashCode()
        result = 31 * result + copyAnnotationExcludes.hashCode()
        return result
    }

    override fun toString(): String {
        return "Transformer(copyAnnotationExcludes=$copyAnnotationExcludes, markAnnotation=$markAnnotation, transformFunctionInfo=$transformFunctionInfo, transformReturnType=$transformReturnType, transformReturnTypeGeneric=$transformReturnTypeGeneric, originFunctionIncludeAnnotations=$originFunctionIncludeAnnotations, syntheticFunctionIncludeAnnotations=$syntheticFunctionIncludeAnnotations, copyAnnotationsToSyntheticFunction=$copyAnnotationsToSyntheticFunction, copyAnnotationsToSyntheticProperty=$copyAnnotationsToSyntheticProperty)"
    }
}

/**
 * 可序列化的配置信息。
 */
@Serializable
class SuspendTransformConfiguration @InternalSuspendTransformConstructorApi constructor(
    val enabled: Boolean,
    val transformers: Map<TargetPlatform, List<Transformer>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SuspendTransformConfiguration) return false

        if (enabled != other.enabled) return false
        if (transformers != other.transformers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + transformers.hashCode()
        return result
    }

    override fun toString(): String {
        return "SuspendTransformConfiguration(enabled=$enabled, transformers=$transformers)"
    }
}

/**
 * Merge both
 */
@InternalSuspendTransformConstructorApi
operator fun SuspendTransformConfiguration.plus(other: SuspendTransformConfiguration): SuspendTransformConfiguration {
    return SuspendTransformConfiguration(
        enabled = enabled && other.enabled,
        transformers = transformers.toMutableMap().apply {
            other.transformers.forEach { (platform, transformers) ->
                compute(platform) { _, old ->
                    if (old == null) transformers.toList() else old + transformers
                }
            }
        }
    )
}

/**
 * Some constants for configuration.
 */
@OptIn(InternalSuspendTransformConstructorApi::class)
object SuspendTransformConfigurations {
    private const val KOTLIN = "kotlin"
    private const val KOTLIN_JVM = "kotlin.jvm"
    private const val KOTLIN_JS = "kotlin.js"

    private const val SUSPENDTRANS_ANNOTATION_PACKAGE = "love.forte.plugin.suspendtrans.annotation"
    private const val SUSPENDTRANS_RUNTIME_PACKAGE = "love.forte.plugin.suspendtrans.runtime"

    private const val JVM_RUN_IN_BLOCKING_FUNCTION_FUNCTION_NAME = "\$runInBlocking\$"
    private const val JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME = "\$runInAsync\$"

    private const val JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME = "\$runInAsync\$"

    //region Commons
    @JvmStatic
    val kotlinOptInClassInfo = ClassInfo(
        packageName = KOTLIN,
        className = "OptIn"
    )
    //endregion

    //region JVM Defaults
    @JvmStatic
    val jvmSyntheticClassInfo = ClassInfo(
        packageName = KOTLIN_JVM,
        className = "JvmSynthetic"
    )

    @JvmStatic
    val jvmApi4JAnnotationClassInfo = ClassInfo(
        packageName = SUSPENDTRANS_ANNOTATION_PACKAGE,
        className = "Api4J"
    )

    @JvmStatic
    val jvmBlockingMarkAnnotationClassInfo = ClassInfo(
        packageName = SUSPENDTRANS_ANNOTATION_PACKAGE,
        className = "JvmBlocking"
    )

    @JvmStatic
    val jvmBlockingAnnotationInfo = MarkAnnotation(
        classInfo = jvmBlockingMarkAnnotationClassInfo,
        defaultSuffix = "Blocking"
    )

    @JvmStatic
    val jvmBlockingTransformFunction = FunctionInfo(
        packageName = SUSPENDTRANS_RUNTIME_PACKAGE,
        functionName = JVM_RUN_IN_BLOCKING_FUNCTION_FUNCTION_NAME,
    )

    @JvmStatic
    val jvmAsyncMarkAnnotationClassInfo = ClassInfo(
        packageName = SUSPENDTRANS_ANNOTATION_PACKAGE,
        className = "JvmAsync"
    )

    @JvmStatic
    val jvmAsyncAnnotationInfo = MarkAnnotation(
        classInfo = jvmAsyncMarkAnnotationClassInfo,
        defaultSuffix = "Async"
    )

    @JvmStatic
    val jvmAsyncTransformFunction = FunctionInfo(
        packageName = SUSPENDTRANS_RUNTIME_PACKAGE,
        functionName = JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
    )

    @JvmStatic
    val jvmBlockingTransformer = Transformer(
        markAnnotation = jvmBlockingAnnotationInfo,
        transformFunctionInfo = jvmBlockingTransformFunction,
        transformReturnType = null,
        transformReturnTypeGeneric = false,
        originFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmSyntheticClassInfo)),
        syntheticFunctionIncludeAnnotations = listOf(
            IncludeAnnotation(
                classInfo = jvmApi4JAnnotationClassInfo,
                includeProperty = true
            )
        ),
        copyAnnotationsToSyntheticFunction = true,
        copyAnnotationExcludes = listOf(
            jvmSyntheticClassInfo,
            jvmBlockingMarkAnnotationClassInfo,
            jvmAsyncMarkAnnotationClassInfo,
            kotlinOptInClassInfo,
        ),
    )

    @JvmStatic
    val jvmAsyncTransformer = Transformer(
        markAnnotation = jvmAsyncAnnotationInfo,
        transformFunctionInfo = jvmAsyncTransformFunction,
        transformReturnType = ClassInfo("java.util.concurrent", "CompletableFuture"),
        transformReturnTypeGeneric = true,
        originFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmSyntheticClassInfo)),
        syntheticFunctionIncludeAnnotations = listOf(
            IncludeAnnotation(jvmApi4JAnnotationClassInfo, includeProperty = true)
        ),
        copyAnnotationsToSyntheticFunction = true,
        copyAnnotationExcludes = listOf(
            jvmSyntheticClassInfo,
            jvmBlockingMarkAnnotationClassInfo,
            jvmAsyncMarkAnnotationClassInfo,
            kotlinOptInClassInfo,
        ),
    )
    //endregion

    //region JS Defaults
    @JvmStatic
    val kotlinJsExportClassInfo = ClassInfo(
        packageName = KOTLIN_JS,
        className = "JsExport"
    )

    @JvmStatic
    val kotlinJsExportIgnoreClassInfo = ClassInfo(
        packageName = KOTLIN_JS,
        className = "JsExport.Ignore"
    )

    @JvmStatic
    val jsApi4JsAnnotationInfo = ClassInfo(
        packageName = SUSPENDTRANS_ANNOTATION_PACKAGE,
        className = "Api4Js"
    )

    @JvmStatic
    val jsAsyncMarkAnnotationClassInfo = ClassInfo(
        packageName = SUSPENDTRANS_ANNOTATION_PACKAGE,
        className = "JsPromise"
    )

    @JvmStatic
    val jsAsyncAnnotationInfo = MarkAnnotation(
        classInfo = jsAsyncMarkAnnotationClassInfo,
        defaultSuffix = "Async"
    )

    @JvmStatic
    val jsAsyncTransformFunction = FunctionInfo(
        SUSPENDTRANS_RUNTIME_PACKAGE,
        JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME,
    )

    @JvmStatic
    val jsPromiseTransformer = Transformer(
        markAnnotation = jsAsyncAnnotationInfo,
        transformFunctionInfo = jsAsyncTransformFunction,
        transformReturnType = ClassInfo(KOTLIN_JS, "Promise"),
        transformReturnTypeGeneric = true,
        originFunctionIncludeAnnotations = listOf(),
        syntheticFunctionIncludeAnnotations = listOf(
            IncludeAnnotation(jsApi4JsAnnotationInfo, includeProperty = true)
        ),
        copyAnnotationsToSyntheticFunction = true,
        copyAnnotationExcludes = listOf(
            jsAsyncMarkAnnotationClassInfo,
            kotlinOptInClassInfo,
        )
    )
    //endregion
}
