package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.configuration.*
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jsPromiseTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmAsyncTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmBlockingTransformer
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.*
import javax.inject.Inject

/**
 * @since 0.12.0
 */
@DslMarker
annotation class SuspendTransformPluginExtensionSpecDslMarker

/**
 * @since 0.12.0
 */
@SuspendTransformPluginExtensionSpecDslMarker
interface SuspendTransformPluginExtensionSpec

/**
 * @since 0.12.0
 */
abstract class TransformerContainer
@Inject constructor(private val objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    internal val transformers: MutableMap<TargetPlatform, ListProperty<TransformerSpec>> = mutableMapOf()

    private fun getTransformers(platform: TargetPlatform): ListProperty<TransformerSpec> {
        return transformers.computeIfAbsent(platform) { objects.listProperty(TransformerSpec::class.java) }
    }

    fun add(platform: TargetPlatform, action: Action<in TransformerSpec>) {
        val listProperty = getTransformers(platform)
        listProperty.add(objects.newInstance(TransformerSpec::class.java).also(action::execute))
    }

    fun add(platform: TargetPlatform, transformer: TransformerSpec) {
        val listProperty = getTransformers(platform)
        listProperty.add(transformer)
    }

    fun add(platform: TargetPlatform, transformer: Provider<TransformerSpec>) {
        val listProperty = getTransformers(platform)
        listProperty.add(transformer)
    }

    fun add(platform: TargetPlatform, transformer: Transformer) {
        add(platform) {
            it.from(transformer)
        }
    }

    fun addJvm(transformer: TransformerSpec) = add(TargetPlatform.JVM, transformer)
    fun addJvm(transformer: Transformer) = addJvm { it.from(transformer) }
    fun addJvm(transformer: Provider<TransformerSpec>) = add(TargetPlatform.JVM, transformer)
    fun addJvm(action: Action<in TransformerSpec>) = add(TargetPlatform.JVM, action)

    fun addJs(transformer: TransformerSpec) = add(TargetPlatform.JS, transformer)
    fun addJs(transformer: Transformer) = addJs { it.from(transformer) }
    fun addJs(transformer: Provider<TransformerSpec>) = add(TargetPlatform.JS, transformer)
    fun addJs(action: Action<in TransformerSpec>) = add(TargetPlatform.JS, action)

    fun addNative(transformer: TransformerSpec) = add(TargetPlatform.NATIVE, transformer)
    fun addNative(transformer: Transformer) = addNative { it.from(transformer) }
    fun addNative(transformer: Provider<TransformerSpec>) = add(TargetPlatform.NATIVE, transformer)
    fun addNative(action: Action<in TransformerSpec>) = add(TargetPlatform.NATIVE, action)

    fun addWasm(transformer: TransformerSpec) = add(TargetPlatform.WASM, transformer)
    fun addWasm(transformer: Transformer) = addWasm { it.from(transformer) }
    fun addWasm(transformer: Provider<TransformerSpec>) = add(TargetPlatform.WASM, transformer)
    fun addWasm(action: Action<in TransformerSpec>) = add(TargetPlatform.WASM, action)

    fun addCommon(transformer: TransformerSpec) = add(TargetPlatform.COMMON, transformer)
    fun addCommon(transformer: Transformer) = addCommon { it.from(transformer) }
    fun addCommon(transformer: Provider<TransformerSpec>) = add(TargetPlatform.COMMON, transformer)
    fun addCommon(action: Action<in TransformerSpec>) = add(TargetPlatform.COMMON, action)


    // JVM defaults
    fun addJvmBlocking() {
        addJvm(jvmBlockingTransformer)
    }

    fun addJvmBlocking(action: Action<in TransformerSpec>) {
        addJvm {
            it.from(jvmBlockingTransformer)
            action.execute(it)
        }
    }

    fun addJvmAsync() {
        addJvm(jvmAsyncTransformer)
    }

    fun addJvmAsync(action: Action<in TransformerSpec>) {
        addJvm {
            it.from(jvmAsyncTransformer)
            action.execute(it)
        }
    }

    // JS defaults

    /**
     * Add [jsPromiseTransformer]
     */
    fun addJsPromise() {
        addJs(jsPromiseTransformer)
    }

    /**
     * Add a js transformer based on [jsPromiseTransformer]
     */
    fun addJsPromise(action: Action<in TransformerSpec>) {
        addJs {
            it.from(jsPromiseTransformer)
            action.execute(it)
        }
    }

    fun useJvmDefault() {
        addJvmBlocking()
        addJvmAsync()
    }

    fun useJsDefault() {
        addJsPromise()
    }

    fun useDefault() {
        useJvmDefault()
        useJsDefault()
    }
}

/**
 * @since 0.12.0
 */
abstract class SuspendTransformPluginExtension
@Inject constructor(objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    /**
     * Enabled plugin.
     *
     * Default is `true`.
     */
    abstract val enabled: Property<Boolean>

    val transformers: TransformerContainer = objects.newInstance()

    fun transformers(action: Action<in TransformerContainer>) {
        action.execute(transformers)
    }

    /**
     * Include the `love.forte.plugin.suspend-transform:suspend-transform-annotation`.
     * Default is `true`.
     */
    abstract val includeAnnotation: Property<Boolean>

    /**
     * Include the `love.forte.plugin.suspend-transform:suspend-transform-runtime`.
     * Default is `true`.
     */
    abstract val includeRuntime: Property<Boolean>

    /**
     * Default is `compileOnly` with [SuspendTransPluginConstants.ANNOTATION_VERSION]
     */
    abstract val annotationDependency: Property<AnnotationDependencySpec>

    fun annotationDependency(action: Action<in AnnotationDependencySpec>) {
        annotationDependency.set(annotationDependency.get().also(action::execute))
    }

    /**
     * Default is `implementation` with [SuspendTransPluginConstants.RUNTIME_VERSION]
     */
    abstract val runtimeDependency: Property<RuntimeDependencySpec>

    fun runtimeDependency(action: Action<in RuntimeDependencySpec>) {
        runtimeDependency.set(runtimeDependency.get().also(action::execute))
    }

    fun runtimeAsApi() {
        runtimeDependency.get().configurationName.set("api")
    }
}

@OptIn(InternalSuspendTransformConstructorApi::class)
internal fun SuspendTransformPluginExtension.toConfiguration(): SuspendTransformConfiguration {
    return SuspendTransformConfiguration(
        enabled = enabled.getOrElse(true),
        transformers = buildMap {
            transformers.transformers.forEach { (k, values) ->
                val list = values.map { valueList -> valueList.map { it.toTransformer() } }.getOrElse(emptyList())
                if (list.isNotEmpty()) {
                    put(k, list)
                }
            }
        },
    )
}

/**
 * @since 0.12.0
 */
sealed interface DependencySpec : SuspendTransformPluginExtensionSpec {
    val version: Property<String>
    val configurationName: Property<String>
}

/**
 * @since 0.12.0
 */
interface AnnotationDependencySpec : DependencySpec {
    /**
     * Default is `compileOnly`.
     */
    override val configurationName: Property<String>

    /**
     * Default is [SuspendTransPluginConstants.ANNOTATION_VERSION]
     */
    override val version: Property<String>
}

/**
 * @since 0.12.0
 */
interface RuntimeDependencySpec : DependencySpec {
    /**
     * Default is `implementation`.
     */
    override val configurationName: Property<String>

    /**
     * Default is [SuspendTransPluginConstants.RUNTIME_VERSION]
     */
    override val version: Property<String>
}

internal fun SuspendTransformPluginExtension.defaults(
    objects: ObjectFactory,
    providers: ProviderFactory
) {
    enabled.convention(true)
    includeAnnotation.convention(true)
    includeRuntime.convention(true)
    annotationDependency.convention(providers.provider {
        objects.newInstance(AnnotationDependencySpec::class.java).apply {
            version.convention(SuspendTransPluginConstants.ANNOTATION_VERSION)
            configurationName.convention("compileOnly")
        }
    })
    runtimeDependency.convention(providers.provider {
        objects.newInstance(RuntimeDependencySpec::class.java).apply {
            version.convention(SuspendTransPluginConstants.RUNTIME_VERSION)
            configurationName.convention("implementation")
        }
    })
}

/**
 * @since 0.12.0
 */
abstract class TransformerSpec
@Inject constructor(private val objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    /**
     * @see Transformer.markAnnotation
     */
    abstract val markAnnotation: Property<MarkAnnotationSpec>

    fun markAnnotation(action: Action<in MarkAnnotationSpec>) {
        val old = markAnnotation.getOrElse(objects.newInstance<MarkAnnotationSpec>())
        markAnnotation.set(old.also(action::execute))
    }

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
    abstract val transformFunctionInfo: Property<FunctionInfoSpec>

    fun transformFunctionInfo(action: Action<in FunctionInfoSpec>) {
        transformFunctionInfo.set(
            transformFunctionInfo.getOrElse(
                objects.newInstance()
            ).also(action::execute)
        )
    }

    /**
     * 转化后的返回值类型, 为null时代表与原函数一致。
     *
     * Will be used when [transformReturnTypeGeneric] is `true`.
     */
    abstract val transformReturnType: Property<ClassInfoSpec>

    fun transformReturnType(action: Action<in ClassInfoSpec>) {
        transformReturnType.set(
            transformReturnType.getOrElse(
                objects.newInstance()
            ).also(action::execute)
        )
    }

    /**
     * 转化后的返回值类型中，是否存在需要与原本返回值类型一致的泛型。
     * 这里指的是返回值类型中嵌套的范型，例如 `CompletableFuture<T>` 中的 `T`。
     * 如果是直接返回 `T`，则不需要设置为 `true`。
     *
     * Default value is `false`.
     */
    val transformReturnTypeGeneric: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    fun transformReturnTypeGeneric() {
        transformReturnTypeGeneric.set(true)
    }

    /**
     * 函数生成后，需要在原函数上追加的注解信息。
     *
     * 例如追加个 `@kotlin.jvm.JvmSynthetic` 之类的。
     */
    abstract val originFunctionIncludeAnnotations: DomainObjectSet<IncludeAnnotationSpec>

    private fun newIncludeAnnotationSpec(): IncludeAnnotationSpec =
        objects.newInstance()

    fun createIncludeAnnotation(action: Action<IncludeAnnotationSpec>): IncludeAnnotationSpec {
        return newIncludeAnnotationSpec().also(action::execute)
    }

    fun addOriginFunctionIncludeAnnotation(action: Action<IncludeAnnotationSpec>) {
        originFunctionIncludeAnnotations.add(
            newIncludeAnnotationSpec().also(action::execute)
        )
    }

    abstract val syntheticFunctionIncludeAnnotations: DomainObjectSet<IncludeAnnotationSpec>

    fun addSyntheticFunctionIncludeAnnotation(action: Action<IncludeAnnotationSpec>) {
        syntheticFunctionIncludeAnnotations.add(createIncludeAnnotation(action))
    }

    /**
     * 是否复制源函数上的注解到新的函数上。
     * 如果生成的是属性类型，则表示是否复制到 `getter` 上。
     *
     * Default value is `false`.
     */
    val copyAnnotationsToSyntheticFunction: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    fun copyAnnotationsToSyntheticFunction() {
        copyAnnotationsToSyntheticFunction.set(true)
    }

    /**
     * 复制原函数上注解时需要排除掉的注解。
     */
    abstract val copyAnnotationExcludes: DomainObjectSet<ClassInfoSpec>

    /**
     * Add a [ClassInfoSpec] into [copyAnnotationExcludes]
     */
    fun addCopyAnnotationExclude(action: Action<in ClassInfoSpec>) {
        copyAnnotationExcludes.add(createCopyAnnotationExclude(action))
    }

    /**
     * Create a [ClassInfoSpec] but does not add.
     */
    fun createCopyAnnotationExclude(action: Action<in ClassInfoSpec>): ClassInfoSpec {
        return objects.newInstance<ClassInfoSpec>().also(action::execute)
    }

    /**
     * 如果是生成属性的话，是否复制源函数上的注解到新的属性上
     *
     * @since 0.9.0
     */
    val copyAnnotationsToSyntheticProperty: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    fun copyAnnotationsToSyntheticProperty() {
        copyAnnotationsToSyntheticProperty.set(true)
    }

    /**
     * Configures the current specification using a [Transformer].
     *
     * @param transformer see [Transformer] or the constants in [SuspendTransformConfigurations],
     * e.g. [SuspendTransformConfigurations.jvmBlockingTransformer].
     */
    fun from(transformer: Transformer) {
        markAnnotation { it.from(transformer.markAnnotation) }
        transformFunctionInfo { it.from(transformer.transformFunctionInfo) }
        transformer.transformReturnType?.also { transformReturnType ->
            transformReturnType { it.from(transformReturnType) }
        }
        transformReturnTypeGeneric.set(transformer.transformReturnTypeGeneric)
        for (originFunctionIncludeAnnotation in transformer.originFunctionIncludeAnnotations) {
            addOriginFunctionIncludeAnnotation {
                it.from(originFunctionIncludeAnnotation)
            }
        }

        for (syntheticFunctionIncludeAnnotation in transformer.syntheticFunctionIncludeAnnotations) {
            addSyntheticFunctionIncludeAnnotation {
                it.from(syntheticFunctionIncludeAnnotation)
            }
        }

        copyAnnotationsToSyntheticFunction.set(transformer.copyAnnotationsToSyntheticFunction)

        for (copyAnnotationExclude in transformer.copyAnnotationExcludes) {
            addCopyAnnotationExclude {
                it.from(copyAnnotationExclude)
            }
        }

        copyAnnotationsToSyntheticProperty.set(transformer.copyAnnotationsToSyntheticProperty)
    }
}

/**
 * @see MarkAnnotation
 */
/**
 * @since 0.12.0
 */
abstract class MarkAnnotationSpec
@Inject constructor(private val objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    /**
     * The mark annotation's class info.
     */
    abstract val classInfo: Property<ClassInfoSpec>

    fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action::execute))
    }

    /**
     * 用于标记生成函数需要使用的基础函数名的注解属性名。
     *
     * Default value is `"baseName"`
     */
    val baseNameProperty: Property<String> =
        objects.property(String::class.java).convention("baseName")

    /**
     * 用于标记生成函数需要使用的基础函数名之后的后缀的注解属性名。
     *
     * Default value is `"suffix"`
     */
    val suffixProperty: Property<String> =
        objects.property(String::class.java).convention("suffix")

    /**
     * 用于标记生成函数是否需要转化为 property 类型的注解属性名。
     *
     * Default value is `"asProperty"`
     */
    val asPropertyProperty: Property<String> =
        objects.property(String::class.java).convention("asProperty")

    /**
     * 当 [suffixProperty] 不存在时使用的默认后缀
     *
     * Default value is `""`
     */
    val defaultSuffix: Property<String> =
        objects.property(String::class.java).convention("")

    /**
     * 当 [asPropertyProperty] 不存在时使用的默认值
     */
    val defaultAsProperty: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    fun from(markAnnotation: MarkAnnotation) {
        classInfo {
            it.from(markAnnotation.classInfo)
        }
        baseNameProperty.set(markAnnotation.baseNameProperty)
        suffixProperty.set(markAnnotation.suffixProperty)
        asPropertyProperty.set(markAnnotation.asPropertyProperty)
        defaultSuffix.set(markAnnotation.defaultSuffix)
        defaultAsProperty.set(markAnnotation.defaultAsProperty)
    }
}

/**
 * @see ClassInfo
 */
/**
 * @since 0.12.0
 */
interface ClassInfoSpec : SuspendTransformPluginExtensionSpec {
    val packageName: Property<String>
    val className: Property<String>

    /**
     * Default value is `false`
     */
    val local: Property<Boolean>

    /**
     * Default value is `false`
     */
    val nullable: Property<Boolean>

    fun from(classInfo: ClassInfo) {
        packageName.set(classInfo.packageName)
        className.set(classInfo.className)
        local.set(classInfo.local)
        nullable.set(classInfo.nullable)
    }
}

/**
 * @since 0.12.0
 */
interface FunctionInfoSpec : SuspendTransformPluginExtensionSpec {
    val packageName: Property<String>
    val functionName: Property<String>

    fun from(functionInfo: FunctionInfo) {
        packageName.set(functionInfo.packageName)
        functionName.set(functionInfo.functionName)
    }
}

/**
 * @since 0.12.0
 */
abstract class IncludeAnnotationSpec
@Inject constructor(private val objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    abstract val classInfo: Property<ClassInfoSpec>

    fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action::execute))
    }

    /**
     * Default value is `false`
     */
    abstract val repeatable: Property<Boolean>

    /**
     * Default value is `false`
     */
    abstract val includeProperty: Property<Boolean>

    fun from(includeAnnotation: IncludeAnnotation) {
        classInfo {
            it.from(includeAnnotation.classInfo)
        }
        repeatable.set(includeAnnotation.repeatable)
        includeProperty.set(includeAnnotation.includeProperty)
    }
}

@OptIn(InternalSuspendTransformConstructorApi::class)
internal fun TransformerSpec.toTransformer(): Transformer {
    return Transformer(
        markAnnotation = markAnnotation.get().toMarkAnnotation(),
        transformFunctionInfo = transformFunctionInfo.get().toFunctionInfo(),
        transformReturnType = transformReturnType.orNull?.toClassInfo(),
        transformReturnTypeGeneric = transformReturnTypeGeneric.getOrElse(false),
        originFunctionIncludeAnnotations = originFunctionIncludeAnnotations.map { it.toIncludeAnnotation() }.toList(),
        syntheticFunctionIncludeAnnotations = syntheticFunctionIncludeAnnotations.map { it.toIncludeAnnotation() }
            .toList(),
        copyAnnotationsToSyntheticFunction = copyAnnotationsToSyntheticFunction.getOrElse(false),
        copyAnnotationExcludes = copyAnnotationExcludes.map { it.toClassInfo() }.toList(),
        copyAnnotationsToSyntheticProperty = copyAnnotationsToSyntheticProperty.get()
    )
}

@OptIn(InternalSuspendTransformConstructorApi::class)
internal fun MarkAnnotationSpec.toMarkAnnotation(): MarkAnnotation {
    return MarkAnnotation(
        classInfo = classInfo.get().toClassInfo(),
        baseNameProperty = baseNameProperty.get(),
        suffixProperty = suffixProperty.get(),
        asPropertyProperty = asPropertyProperty.get(),
        defaultSuffix = defaultSuffix.get(),
        defaultAsProperty = defaultAsProperty.get()
    )
}

@OptIn(InternalSuspendTransformConstructorApi::class)
internal fun ClassInfoSpec.toClassInfo(): ClassInfo {
    return ClassInfo(
        packageName = packageName.get(),
        className = className.get(),
        local = local.getOrElse(false),
        nullable = nullable.getOrElse(false)
    )
}

@OptIn(InternalSuspendTransformConstructorApi::class)
internal fun FunctionInfoSpec.toFunctionInfo(): FunctionInfo {
    return FunctionInfo(
        packageName = packageName.get(),
        functionName = functionName.get()
    )
}

@OptIn(InternalSuspendTransformConstructorApi::class)
internal fun IncludeAnnotationSpec.toIncludeAnnotation(): IncludeAnnotation {
    return IncludeAnnotation(
        classInfo = classInfo.get().toClassInfo(),
        repeatable = repeatable.getOrElse(false),
        includeProperty = includeProperty.getOrElse(false)
    )
}

private inline fun <reified T : Any> ObjectFactory.newInstance(): T = newInstance(T::class.java)
