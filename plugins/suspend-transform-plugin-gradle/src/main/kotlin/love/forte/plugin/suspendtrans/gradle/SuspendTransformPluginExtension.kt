package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.configuration.*
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jsPromiseTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmAsyncTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmBlockingTransformer
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.util.*
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
 * SuspendTransform plugin extension class info specification interface.
 * Used for configuring class information related functionality.
 *
 * @since 0.12.0
 */
@SuspendTransformPluginExtensionSpecDslMarker
interface SuspendTransformPluginExtensionClassInfoSpec : SuspendTransformPluginExtensionSpec {
    fun classInfo(action: Action<in ClassInfoSpec>)
    fun classInfo(action: ClassInfoSpec.() -> Unit)
}

/**
 * @since 0.12.0
 */
@Suppress("unused")
interface SuspendTransformPluginExtensionSpecFactory {
    fun createClassInfo(action: Action<in ClassInfoSpec>): ClassInfoSpec
    fun createClassInfo(action: ClassInfoSpec.() -> Unit): ClassInfoSpec =
        createClassInfo(Action(action))

    fun createClassInfo(): ClassInfoSpec =
        createClassInfo { }

    fun createMarkAnnotation(action: Action<in MarkAnnotationSpec>): MarkAnnotationSpec
    fun createMarkAnnotation(action: MarkAnnotationSpec.() -> Unit): MarkAnnotationSpec =
        createMarkAnnotation(Action(action))

    fun createMarkAnnotation(): MarkAnnotationSpec =
        createMarkAnnotation { }

    fun createFunctionInfo(action: Action<in FunctionInfoSpec>): FunctionInfoSpec
    fun createFunctionInfo(action: FunctionInfoSpec.() -> Unit): FunctionInfoSpec =
        createFunctionInfo(Action(action))

    fun createFunctionInfo(): FunctionInfoSpec =
        createFunctionInfo { }

    fun createIncludeAnnotation(action: Action<in IncludeAnnotationSpec>): IncludeAnnotationSpec
    fun createIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit): IncludeAnnotationSpec =
        createIncludeAnnotation(Action(action))

    fun createIncludeAnnotation(): IncludeAnnotationSpec =
        createIncludeAnnotation { }

    fun createRuntimeDependency(action: Action<in RuntimeDependencySpec>): RuntimeDependencySpec
    fun createRuntimeDependency(action: RuntimeDependencySpec.() -> Unit): RuntimeDependencySpec =
        createRuntimeDependency(Action(action))

    fun createRuntimeDependency(): RuntimeDependencySpec =
        createRuntimeDependency { }

    fun createAnnotationDependency(action: Action<in AnnotationDependencySpec>): AnnotationDependencySpec
    fun createAnnotationDependency(action: AnnotationDependencySpec.() -> Unit): AnnotationDependencySpec =
        createAnnotationDependency(Action(action))

    fun createAnnotationDependency(): AnnotationDependencySpec =
        createAnnotationDependency { }

    fun createTransformFunctionInfo(action: Action<in FunctionInfoSpec>): FunctionInfoSpec
    fun createTransformFunctionInfo(action: FunctionInfoSpec.() -> Unit): FunctionInfoSpec =
        createTransformFunctionInfo(Action(action))

    fun createTransformFunctionInfo(): FunctionInfoSpec =
        createTransformFunctionInfo { }

    fun createTransformer(action: Action<in TransformerSpec>): TransformerSpec
    fun createTransformer(action: TransformerSpec.() -> Unit): TransformerSpec =
        createTransformer(Action(action))

    fun createTransformer(): TransformerSpec =
        createTransformer { }
}

/**
 * @since 0.12.0
 */
interface SuspendTransformPluginExtensionSpecFactoryAware {
    val factory: SuspendTransformPluginExtensionSpecFactory
}

private class SuspendTransformPluginExtensionSpecFactoryImpl(
    private val objects: ObjectFactory,
) : SuspendTransformPluginExtensionSpecFactory {
    override fun createClassInfo(action: Action<in ClassInfoSpec>): ClassInfoSpec {
        return objects.newInstance<ClassInfoSpec>().also(action::execute)
    }

    override fun createAnnotationDependency(action: Action<in AnnotationDependencySpec>): AnnotationDependencySpec {
        return objects.newInstance<AnnotationDependencySpec>().also(action::execute)
    }

    override fun createMarkAnnotation(action: Action<in MarkAnnotationSpec>): MarkAnnotationSpec {
        return objects.newInstance<MarkAnnotationSpec>().also(action::execute)
    }

    override fun createFunctionInfo(action: Action<in FunctionInfoSpec>): FunctionInfoSpec {
        return objects.newInstance<FunctionInfoSpec>().also(action::execute)
    }

    override fun createIncludeAnnotation(action: Action<in IncludeAnnotationSpec>): IncludeAnnotationSpec {
        return objects.newInstance<IncludeAnnotationSpec>().also(action::execute)
    }

    override fun createRuntimeDependency(action: Action<in RuntimeDependencySpec>): RuntimeDependencySpec {
        return objects.newInstance<RuntimeDependencySpec>().also(action::execute)
    }

    override fun createTransformFunctionInfo(action: Action<in FunctionInfoSpec>): FunctionInfoSpec {
        return objects.newInstance<FunctionInfoSpec>().also(action::execute)
    }

    override fun createTransformer(action: Action<in TransformerSpec>): TransformerSpec {
        return objects.newInstance<TransformerSpec>().also(action::execute)
    }
}

/**
 * @since 0.12.0
 */
@Suppress("unused")
abstract class TransformersContainer
@Inject constructor(
    private val objects: ObjectFactory
) : SuspendTransformPluginExtensionSpec,
    SuspendTransformPluginExtensionSpecFactoryAware {
    override val factory: SuspendTransformPluginExtensionSpecFactory =
        SuspendTransformPluginExtensionSpecFactoryImpl(objects)

    //     mutableMapOf()
    internal val containers: MutableMap<TargetPlatform, ListProperty<TransformerSpec>> =
        EnumMap(TargetPlatform::class.java)
    // TODO Maybe ...
    //  containers: NamedDomainObjectContainer<TransformerSpecContainer> =
    //     objects.domainObjectContainer(TransformerSpecContainer::class.java) { name ->
    //         objects.newInstance(TransformerSpecContainer::class.java, name)
    //     }
    //  abstract class TransformerSpecContainer @Inject constructor(name: String) {
    //      val targetPlatform: TargetPlatform = TargetPlatform.valueOf(name)
    //      abstract val transformerSet: DomainObjectSet<TransformerSpec>
    //  }


    private fun getTransformersInternal(platform: TargetPlatform): ListProperty<TransformerSpec> {
        // return containers.maybeCreate(platform.name).transformerSet
        return containers.computeIfAbsent(platform) { objects.listProperty(TransformerSpec::class.java) }
    }

    /**
     * Create a [TransformerSpec] but not add.
     */
    fun createTransformer(action: Action<in TransformerSpec>): TransformerSpec {
        return objects.newInstance<TransformerSpec>().also(action::execute)
    }

    /**
     * Create a [TransformerSpec] but not add.
     */
    fun createTransformer(action: TransformerSpec.() -> Unit): TransformerSpec {
        return factory.createTransformer(action)
    }

    fun add(platform: TargetPlatform, action: Action<in TransformerSpec>) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(createTransformer(action))
    }

    fun add(platform: TargetPlatform, action: TransformerSpec.() -> Unit) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(createTransformer(action))
    }

    fun add(platform: TargetPlatform, transformer: TransformerSpec) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(transformer)
    }

    fun add(platform: TargetPlatform, transformer: Provider<TransformerSpec>) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(transformer)
    }

    fun add(platform: TargetPlatform, transformer: Transformer) {
        add(platform) {
            from(transformer)
        }
    }

    fun addJvm(transformer: TransformerSpec) = add(TargetPlatform.JVM, transformer)
    fun addJvm(transformer: Transformer) = addJvm { from(transformer) }
    fun addJvm(transformer: Provider<TransformerSpec>) = add(TargetPlatform.JVM, transformer)
    fun addJvm(action: Action<in TransformerSpec>) = add(TargetPlatform.JVM, action)
    fun addJvm(action: TransformerSpec.() -> Unit) = add(TargetPlatform.JVM, action)

    fun addJs(transformer: TransformerSpec) = add(TargetPlatform.JS, transformer)
    fun addJs(transformer: Transformer) = addJs { from(transformer) }
    fun addJs(transformer: Provider<TransformerSpec>) = add(TargetPlatform.JS, transformer)
    fun addJs(action: Action<in TransformerSpec>) = add(TargetPlatform.JS, action)
    fun addJs(action: TransformerSpec.() -> Unit) = add(TargetPlatform.JS, action)

    fun addNative(transformer: TransformerSpec) = add(TargetPlatform.NATIVE, transformer)
    fun addNative(transformer: Transformer) = addNative { from(transformer) }
    fun addNative(transformer: Provider<TransformerSpec>) = add(TargetPlatform.NATIVE, transformer)
    fun addNative(action: Action<in TransformerSpec>) = add(TargetPlatform.NATIVE, action)
    fun addNative(action: TransformerSpec.() -> Unit) = add(TargetPlatform.NATIVE, action)

    fun addWasm(transformer: TransformerSpec) = add(TargetPlatform.WASM, transformer)
    fun addWasm(transformer: Transformer) = addWasm { from(transformer) }
    fun addWasm(transformer: Provider<TransformerSpec>) = add(TargetPlatform.WASM, transformer)
    fun addWasm(action: Action<in TransformerSpec>) = add(TargetPlatform.WASM, action)
    fun addWasm(action: TransformerSpec.() -> Unit) = add(TargetPlatform.WASM, action)

    fun addCommon(transformer: TransformerSpec) = add(TargetPlatform.COMMON, transformer)
    fun addCommon(transformer: Transformer) = addCommon { from(transformer) }
    fun addCommon(transformer: Provider<TransformerSpec>) = add(TargetPlatform.COMMON, transformer)
    fun addCommon(action: TransformerSpec.() -> Unit) = add(TargetPlatform.COMMON, action)


    // JVM defaults
    fun addJvmBlocking() {
        addJvm(jvmBlockingTransformer)
    }

    fun addJvmBlocking(action: Action<in TransformerSpec>) {
        addJvm {
            from(jvmBlockingTransformer)
            action.execute(this)
        }
    }

    fun addJvmBlocking(action: TransformerSpec.() -> Unit) {
        addJvm {
            from(jvmBlockingTransformer)
            action()
        }
    }

    fun addJvmAsync() {
        addJvm(jvmAsyncTransformer)
    }

    fun addJvmAsync(action: Action<in TransformerSpec>) {
        addJvm {
            from(jvmAsyncTransformer)
            action.execute(this)
        }
    }

    fun addJvmAsync(action: TransformerSpec.() -> Unit) {
        addJvm {
            from(jvmAsyncTransformer)
            action()
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
    fun addJsPromise(action: TransformerSpec.() -> Unit) {
        addJs {
            from(jsPromiseTransformer)
            action()
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
@Suppress("unused")
abstract class SuspendTransformPluginExtension
@Inject constructor(objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    /**
     * Enabled plugin.
     *
     * Default is `true`.
     */
    abstract val enabled: Property<Boolean>

    val transformers: TransformersContainer = objects.newInstance()

    fun transformers(action: Action<in TransformersContainer>) {
        action.execute(transformers)
    }

    fun transformers(action: TransformersContainer.() -> Unit) {
        action(transformers)
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

    fun annotationDependency(action: AnnotationDependencySpec.() -> Unit) {
        annotationDependency.set(annotationDependency.get().also(action))
    }

    /**
     * Default is `implementation` with [SuspendTransPluginConstants.RUNTIME_VERSION]
     */
    abstract val runtimeDependency: Property<RuntimeDependencySpec>

    fun runtimeDependency(action: Action<in RuntimeDependencySpec>) {
        runtimeDependency.set(runtimeDependency.get().also(action::execute))
    }

    fun runtimeDependency(action: RuntimeDependencySpec.() -> Unit) {
        runtimeDependency.set(runtimeDependency.get().also(action))
    }

    fun runtimeAsApi() {
        runtimeDependency.get().configurationName.set("api")
    }
}

internal data class TransformerEntry(
    val targetPlatform: TargetPlatform,
    val transformers: List<Transformer>
)

@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun SuspendTransformPluginExtension.toConfigurationProvider(objects: ObjectFactory): Provider<SuspendTransformConfiguration> {
    val combines = objects.listProperty(TransformerEntry::class.java)
    for ((targetPlatform, transformerListProperty) in transformers.containers) {
        combines.addAll(
            transformerListProperty.map { list ->
                if (list.isEmpty()) {
                    // Type mismatch: inferred type is TransformerEntry? but TransformerEntry was expected
                    //  if return null with `combines.add`
                    emptyList()
                } else {
                    listOf(
                        TransformerEntry(targetPlatform, list.map { it.toTransformer() })
                    )
                }
            }
        )
    }

    return combines.map { entries ->
        val transformersMap: Map<TargetPlatform, List<Transformer>> = entries.associateBy(
            keySelector = { it.targetPlatform },
            valueTransform = { it.transformers }
        )

        // 此处 `Map` 可能为 空，但是 `List` 不会有空的。
        // 后续在使用的时候只需要判断一下 transformers 本身是不是空即可。
        SuspendTransformConfiguration(
            transformers = transformersMap
        )
    }

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
@Suppress("unused")
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

    fun markAnnotation(action: MarkAnnotationSpec.() -> Unit) {
        val old = markAnnotation.getOrElse(objects.newInstance<MarkAnnotationSpec>())
        markAnnotation.set(old.also(action))
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
    @Suppress("KDocUnresolvedReference")
    abstract val transformFunctionInfo: Property<FunctionInfoSpec>

    fun transformFunctionInfo(action: Action<in FunctionInfoSpec>) {
        transformFunctionInfo.set(
            transformFunctionInfo.getOrElse(
                objects.newInstance()
            ).also(action::execute)
        )
    }

    fun transformFunctionInfo(action: FunctionInfoSpec.() -> Unit) {
        transformFunctionInfo.set(
            transformFunctionInfo.getOrElse(
                objects.newInstance()
            ).also(action)
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

    fun transformReturnType(action: ClassInfoSpec.() -> Unit) {
        transformReturnType.set(
            transformReturnType.getOrElse(
                objects.newInstance()
            ).also(action)
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

    fun createIncludeAnnotation(action: Action<in IncludeAnnotationSpec>): IncludeAnnotationSpec {
        return newIncludeAnnotationSpec().also(action::execute)
    }

    fun createIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit): IncludeAnnotationSpec {
        return newIncludeAnnotationSpec().also(action)
    }

    fun addOriginFunctionIncludeAnnotation(action: Action<in IncludeAnnotationSpec>) {
        originFunctionIncludeAnnotations.add(
            newIncludeAnnotationSpec().also(action::execute)
        )
    }

    fun addOriginFunctionIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit) {
        originFunctionIncludeAnnotations.add(
            newIncludeAnnotationSpec().also(action)
        )
    }

    abstract val syntheticFunctionIncludeAnnotations: DomainObjectSet<IncludeAnnotationSpec>

    fun addSyntheticFunctionIncludeAnnotation(action: Action<in IncludeAnnotationSpec>) {
        syntheticFunctionIncludeAnnotations.add(createIncludeAnnotation(action))
    }

    fun addSyntheticFunctionIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit) {
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
     * Add a [ClassInfoSpec] into [copyAnnotationExcludes]
     */
    fun addCopyAnnotationExclude(action: ClassInfoSpec.() -> Unit) {
        copyAnnotationExcludes.add(createCopyAnnotationExclude(action))
    }

    /**
     * Create a [ClassInfoSpec] but does not add.
     */
    fun createCopyAnnotationExclude(action: Action<in ClassInfoSpec>): ClassInfoSpec {
        return objects.newInstance<ClassInfoSpec>().also(action::execute)
    }

    /**
     * Create a [ClassInfoSpec] but does not add.
     */
    fun createCopyAnnotationExclude(action: ClassInfoSpec.() -> Unit): ClassInfoSpec {
        return objects.newInstance<ClassInfoSpec>().also(action)
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
        markAnnotation { from(transformer.markAnnotation) }
        transformFunctionInfo { from(transformer.transformFunctionInfo) }
        transformer.transformReturnType?.also { transformReturnType ->
            transformReturnType { from(transformReturnType) }
        }
        transformReturnTypeGeneric.set(transformer.transformReturnTypeGeneric)
        for (originFunctionIncludeAnnotation in transformer.originFunctionIncludeAnnotations) {
            addOriginFunctionIncludeAnnotation {
                from(originFunctionIncludeAnnotation)
            }
        }

        for (syntheticFunctionIncludeAnnotation in transformer.syntheticFunctionIncludeAnnotations) {
            addSyntheticFunctionIncludeAnnotation {
                from(syntheticFunctionIncludeAnnotation)
            }
        }

        copyAnnotationsToSyntheticFunction.set(transformer.copyAnnotationsToSyntheticFunction)

        for (copyAnnotationExclude in transformer.copyAnnotationExcludes) {
            addCopyAnnotationExclude {
                from(copyAnnotationExclude)
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
@Suppress("unused")
abstract class MarkAnnotationSpec
@Inject constructor(private val objects: ObjectFactory) :
    SuspendTransformPluginExtensionSpec,
    SuspendTransformPluginExtensionClassInfoSpec {
    /**
     * The mark annotation's class info.
     */
    abstract val classInfo: Property<ClassInfoSpec>

    override fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action::execute))
    }

    override fun classInfo(action: ClassInfoSpec.() -> Unit) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action))
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

    /**
     * @since 0.13.0
     */
    abstract val markNameProperty: Property<MarkNamePropertySpec>

    fun markNameProperty(action: Action<out MarkNamePropertySpec>) {
        markNameProperty.set(markNameProperty.getOrElse(objects.newInstance<MarkNamePropertySpec>()))
    }

    fun from(markAnnotation: MarkAnnotation) {
        classInfo {
            from(markAnnotation.classInfo)
        }
        baseNameProperty.set(markAnnotation.baseNameProperty)
        suffixProperty.set(markAnnotation.suffixProperty)
        asPropertyProperty.set(markAnnotation.asPropertyProperty)
        defaultSuffix.set(markAnnotation.defaultSuffix)
        defaultAsProperty.set(markAnnotation.defaultAsProperty)
    }
}

/**
 * Specification interface for class name information.
 * Used to define properties for package name and class name.
 *
 * @since 0.13.0
 */
interface ClassNameSpec : SuspendTransformPluginExtensionSpec {
    val packageName: Property<String>
    val className: Property<String>

    fun from(classInfo: ClassInfo) {
        packageName.set(classInfo.packageName)
        className.set(classInfo.className)
    }
}

/**
 * @see ClassInfo
 * @since 0.12.0
 */
interface ClassInfoSpec : SuspendTransformPluginExtensionSpec, ClassNameSpec {
    override val packageName: Property<String>
    override val className: Property<String>

    /**
     * Default value is `false`
     */
    val local: Property<Boolean>

    /**
     * Default value is `false`
     */
    val nullable: Property<Boolean>

    override fun from(classInfo: ClassInfo) {
        packageName.set(classInfo.packageName)
        className.set(classInfo.className)
        local.set(classInfo.local)
        nullable.set(classInfo.nullable)
    }
}

/**
 * @since 0.13.0
 */
abstract class MarkNamePropertySpec
@Inject constructor(private val objects: ObjectFactory) :
    SuspendTransformPluginExtensionSpec {
    /**
     * The property name for `markName` in [MarkAnnotation],
     * e.g. `markName` in `@JvmBlocking(markName = "...")`.
     *
     * Default is `"markName"`.
     */
    abstract val propertyName: Property<String>

    /**
     * The name marker annotation.
     */
    abstract val annotation: Property<MarkNameAnnotationSpec>

    fun annotation(action: Action<in MarkNameAnnotationSpec>) {
        annotation.set(annotation.getOrElse(objects.newInstance<MarkNameAnnotationSpec>()).also(action::execute))
    }

    fun annotation(action: MarkNameAnnotationSpec.() -> Unit) {
        annotation.set(annotation.getOrElse(objects.newInstance<MarkNameAnnotationSpec>()).also(action))
    }

    fun from(markNameProperty: MarkNameProperty) {
        propertyName.set(markNameProperty.propertyName)
        annotation {
            from(markNameProperty.annotation)
            propertyName.set(markNameProperty.annotationMarkNamePropertyName)
        }
    }

    companion object {
        const val DEFAULT_PROPERTY_NAME: String = "markName"
    }
}

/**
 * A specification for name marker annotation.
 *
 * @see MarkNameProperty
 *
 * @since 0.13.0
 */
abstract class MarkNameAnnotationSpec
@Inject constructor(private val objects: ObjectFactory) :
    SuspendTransformPluginExtensionSpec, ClassNameSpec {
    abstract override val className: Property<String>
    abstract override val packageName: Property<String>

    /**
     * The name's property name,
     * e.g. `name` of `@JsName(name = "...")`, `name` of `@JvmName(name = "...")`, etc.
     */
    abstract val propertyName: Property<String>
}

/**
 * Function information specification interface for configuring function-related properties
 *
 * @property packageName The package name property
 * @property functionName The function name property
 * @since 0.12.0
 */
interface FunctionInfoSpec : SuspendTransformPluginExtensionSpec {
    val packageName: Property<String>
    val functionName: Property<String>

    /**
     * Configures the specification from an existing [FunctionInfo] instance
     */
    fun from(functionInfo: FunctionInfo) {
        packageName.set(functionInfo.packageName)
        functionName.set(functionInfo.functionName)
    }
}

/**
 * @since 0.12.0
 */
@Suppress("unused")
abstract class IncludeAnnotationSpec
@Inject constructor(private val objects: ObjectFactory) :
    SuspendTransformPluginExtensionSpec,
    SuspendTransformPluginExtensionClassInfoSpec {
    abstract val classInfo: Property<ClassInfoSpec>

    override fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action::execute))
    }

    override fun classInfo(action: ClassInfoSpec.() -> Unit) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action))
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
            from(includeAnnotation.classInfo)
        }
        repeatable.set(includeAnnotation.repeatable)
        includeProperty.set(includeAnnotation.includeProperty)
    }
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
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

@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun MarkAnnotationSpec.toMarkAnnotation(): MarkAnnotation {
    return MarkAnnotation(
        classInfo = classInfo.get().toClassInfo(),
        baseNameProperty = baseNameProperty.get(),
        suffixProperty = suffixProperty.get(),
        asPropertyProperty = asPropertyProperty.get(),
        defaultSuffix = defaultSuffix.get(),
        defaultAsProperty = defaultAsProperty.get(),
        markNameProperty = markNameProperty.orNull?.toMarkNameProperty()
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun MarkNameAnnotationSpec.toClassInfo(): ClassInfo {
    return ClassInfo(
        packageName = packageName.get(),
        className = className.get(),
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun MarkNamePropertySpec.toMarkNameProperty(): MarkNameProperty {
    val annotation = annotation.get()
    return MarkNameProperty(
        propertyName = propertyName.getOrElse(MarkNamePropertySpec.DEFAULT_PROPERTY_NAME),
        annotation = annotation.toClassInfo(),
        annotationMarkNamePropertyName = annotation.propertyName.get()
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun ClassInfoSpec.toClassInfo(): ClassInfo {
    return ClassInfo(
        packageName = packageName.get(),
        className = className.get(),
        local = local.getOrElse(false),
        nullable = nullable.getOrElse(false)
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun FunctionInfoSpec.toFunctionInfo(): FunctionInfo {
    return FunctionInfo(
        packageName = packageName.get(),
        functionName = functionName.get()
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun IncludeAnnotationSpec.toIncludeAnnotation(): IncludeAnnotation {
    return IncludeAnnotation(
        classInfo = classInfo.get().toClassInfo(),
        repeatable = repeatable.getOrElse(false),
        includeProperty = includeProperty.getOrElse(false)
    )
}

internal inline fun <reified T : Any> ObjectFactory.newInstance(): T = newInstance(T::class.java)
