package love.forte.plugin.suspendtrans.configuration

import kotlinx.serialization.Serializable

// NOTE:
//   配置信息均使用 `Protobuf` 进行序列化
//   虽然序列化行为是内部的，但是还是应该尽可能避免出现字段的顺序错乱或删改。

@RequiresOptIn(
    "This is an internal suspend transform configuration's api. " +
            "It may be changed in the future without notice.", RequiresOptIn.Level.ERROR
)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class InternalSuspendTransformConfigurationApi

@Serializable
class FunctionInfo @InternalSuspendTransformConfigurationApi constructor(
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
class ClassInfo @InternalSuspendTransformConfigurationApi constructor(
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
 * 用于标记的注解信息, 例如 `@JvmBlocking`, `@JvmAsync`, `@JsPromise`.
 */
@Serializable
class MarkAnnotation @InternalSuspendTransformConfigurationApi constructor(
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

    /**
     * A name marker like `@JsName`, `@JvmName`, etc.
     *
     * @since 0.13.0
     */
    // 'null' is not supported for optional properties in ProtoBuf
    val markNameProperty: MarkNameProperty?,
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
        if (markNameProperty != other.markNameProperty) return false

        return true
    }

    override fun hashCode(): Int {
        var result = defaultAsProperty.hashCode()
        result = 31 * result + classInfo.hashCode()
        result = 31 * result + baseNameProperty.hashCode()
        result = 31 * result + suffixProperty.hashCode()
        result = 31 * result + asPropertyProperty.hashCode()
        result = 31 * result + defaultSuffix.hashCode()
        result = 31 * result + (markNameProperty?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MarkAnnotation(asPropertyProperty='$asPropertyProperty', classInfo=$classInfo, baseNameProperty='$baseNameProperty', suffixProperty='$suffixProperty', defaultSuffix='$defaultSuffix', defaultAsProperty=$defaultAsProperty, markName=$markNameProperty)"
    }
}

/**
 * The `markName`'s information.
 * @since 0.13.0
 */
@Serializable
class MarkNameProperty @InternalSuspendTransformConfigurationApi constructor(
    /**
     * The `markName`'s property name of the mark annotation.
     * e.g. `markName` of `@JsPromise(markName = "foo")
     */
    val propertyName: String,
    /**
     * The name marker annotation's class info,
     * e.g. `@JsName`, `@JvmName`, etc.
     */
    val annotation: ClassInfo,
    /**
     * The name property of name marker annotation,
     * e.g. `name` of `@JsName(name = "...")`.
     */
    val annotationMarkNamePropertyName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarkNameProperty) return false

        if (propertyName != other.propertyName) return false
        if (annotation != other.annotation) return false
        if (annotationMarkNamePropertyName != other.annotationMarkNamePropertyName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = propertyName.hashCode()
        result = 31 * result + annotation.hashCode()
        result = 31 * result + annotationMarkNamePropertyName.hashCode()
        return result
    }

    override fun toString(): String {
        return "MarkName(annotation=$annotation, propertyName='$propertyName', annotationMarkNamePropertyName='$annotationMarkNamePropertyName')"
    }
}

@Serializable
class IncludeAnnotation @InternalSuspendTransformConfigurationApi constructor(
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
class Transformer @InternalSuspendTransformConfigurationApi constructor(
    /**
     * Information about the marker annotation that marks functions to be transformed.
     */
    val markAnnotation: MarkAnnotation,

    /**
     * Information of the transform function.
     *
     * The actual format of this function must be:
     *
     * ```kotlin
     * fun <T> <fun-name>(block: suspend () -> T[, scope: CoroutineScope? = ...]): T {
     *     // ...
     * }
     * ```
     *
     * Among them, this asynchronous function can have a second parameter, which must be of type [kotlinx.coroutines.CoroutineScope] and is recommended to be nullable.
     * If this parameter exists, when the type containing the transform function implements [kotlinx.coroutines.CoroutineScope] itself, it will fill in itself as a parameter, similar to:
     *
     * ```kotlin
     * class Bar : CoroutineScope {
     *    @Xxx // your custom transform function's mark annotation
     *    suspend fun foo(): Foo
     *
     *    @Api4J fun fooXxx(): CompletableFuture<Foo> = transform(block = { foo() }, scope = this)
     * }
     * ```
     *
     * If the scope parameter is nullable, then the effect is similar to:
     *
     * ```kotlin
     * class Bar {
     *    @Xxx // your custom transform function's mark annotation
     *    suspend fun foo(): Foo
     *
     *    @Api4J fun fooXxx(): CompletableFuture<Foo> = transform(block = { foo() }, scope = this as? CoroutineScope)
     * }
     * ```
     * Therefore, when nullable it is more inheritance-friendly - when the subclass extends CoroutineScope the parameter can take effect, regardless of whether it overrides this function.
     *
     */
    val transformFunctionInfo: FunctionInfo,

    /**
     * The return type after transformation. When null, it represents the same as the original function.
     */
    val transformReturnType: ClassInfo?,

    // TODO TypeGeneric for suspend function return type and transform function return type?

    /**
     * Whether there are generics in the transformed return type that need to be consistent with the original return type.
     */
    val transformReturnTypeGeneric: Boolean,

    /**
     * Annotation information that needs to be added to the original function after function generation.
     *
     * For example, add something like `@kotlin.jvm.JvmSynthetic`.
     */
    val originFunctionIncludeAnnotations: List<IncludeAnnotation>,

    /**
     * Annotation information that needs to be added to the generated function. (No need to specify `@Generated`)
     */
    val syntheticFunctionIncludeAnnotations: List<IncludeAnnotation>,

    /**
     * Whether to copy annotations from the source function to the new function.
     * If generated as a property type, it indicates whether to copy to the `getter`.
     */
    val copyAnnotationsToSyntheticFunction: Boolean,

    /**
     * Annotations to be excluded when copying annotations from the original function.
     */
    val copyAnnotationExcludes: List<ClassInfo>,

    /**
     * If generating a property, whether to copy annotations from the source function to the new property
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
class SuspendTransformConfiguration @InternalSuspendTransformConfigurationApi constructor(
    /**
     * The transformers.
     *
     * Note: This `Map` cannot be empty.
     * The `List` values cannot be empty.
     */
    val transformers: Map<TargetPlatform, List<Transformer>>
) {

    override fun toString(): String {
        return "SuspendTransformConfiguration(transformers=$transformers)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SuspendTransformConfiguration) return false

        if (transformers != other.transformers) return false

        return true
    }

    override fun hashCode(): Int {
        return transformers.hashCode()
    }
}

/**
 * Merge both
 */
@InternalSuspendTransformConfigurationApi
operator fun SuspendTransformConfiguration.plus(other: SuspendTransformConfiguration): SuspendTransformConfiguration {
    return SuspendTransformConfiguration(
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
@OptIn(InternalSuspendTransformConfigurationApi::class)
object SuspendTransformConfigurations {
    private const val KOTLIN = "kotlin"
    private const val KOTLIN_JVM = "kotlin.jvm"
    private const val KOTLIN_JS = "kotlin.js"

    private const val SUSPENDTRANS_ANNOTATION_PACKAGE = "love.forte.plugin.suspendtrans.annotation"
    private const val SUSPENDTRANS_RUNTIME_PACKAGE = "love.forte.plugin.suspendtrans.runtime"

    private const val JVM_RUN_IN_BLOCKING_FUNCTION_FUNCTION_NAME = "\$runInBlocking$"
    private const val JVM_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME = "\$runInAsync$"

    private const val JS_RUN_IN_ASYNC_FUNCTION_FUNCTION_NAME = "\$runInAsync$"

    //region Commons
    @JvmStatic
    val kotlinOptInClassInfo = ClassInfo(
        packageName = KOTLIN,
        className = "OptIn"
    )
    //endregion

    //region JVM Defaults

    /**
     * The `kotlin.jvm.JvmName`.
     * @since 0.13.0
     */
    @JvmStatic
    val jvmNameAnnotationClassInfo = ClassInfo(
        packageName = KOTLIN_JVM,
        className = "JvmName"
    )

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
        defaultSuffix = "Blocking",
        markNameProperty = MarkNameProperty(
            propertyName = "markName",
            annotation = jvmNameAnnotationClassInfo,
            annotationMarkNamePropertyName = "name"
        )
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
        defaultSuffix = "Async",
        markNameProperty = MarkNameProperty(
            propertyName = "markName",
            annotation = jvmNameAnnotationClassInfo,
            annotationMarkNamePropertyName = "name"
        )
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
            jvmNameAnnotationClassInfo,
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
            jvmNameAnnotationClassInfo,
        ),
    )
    //endregion

    //region JS Defaults

    /**
     * The `kotlin.js.JsName`.
     * @since 0.13.0
     */
    @JvmStatic
    val jsNameAnnotationClassInfo = ClassInfo(
        packageName = KOTLIN_JS,
        className = "JsName"
    )

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
        defaultSuffix = "Async",
        markNameProperty = MarkNameProperty(
            propertyName = "markName",
            annotation = jsNameAnnotationClassInfo,
            annotationMarkNamePropertyName = "name"
        )
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
            jsNameAnnotationClassInfo,
        )
    )
    //endregion
}
