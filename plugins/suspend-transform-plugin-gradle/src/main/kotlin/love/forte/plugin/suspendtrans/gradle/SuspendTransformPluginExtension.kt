package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.configuration.*
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.*
import javax.inject.Inject

abstract class TransformerContainer
@Inject constructor(private val objects: ObjectFactory) {
    internal val transformers: MutableMap<TargetPlatform, ListProperty<TransformerSpec>> = mutableMapOf()

    private fun getTransformers(platform: TargetPlatform): ListProperty<TransformerSpec> =
        transformers.computeIfAbsent(platform) { objects.listProperty(TransformerSpec::class.java) }

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

    fun addJvmDefaults() {

    }
}


interface SuspendTransformPluginExtension {
    val enabled: Property<Boolean>

    val transformers: TransformerContainer

    fun transformers(action: Action<in TransformerContainer>) {
        action.execute(transformers)
    }

    fun transformers(block: TransformerContainer.() -> Unit) {
        transformers.block()
    }

    fun useJvmDefault() {
        transformers.addJvm(SuspendTransformConfigurations.jvmBlockingTransformer)
        transformers.addJvm(SuspendTransformConfigurations.jvmAsyncTransformer)
    }

    fun useJsDefault() {
        transformers.addJs(SuspendTransformConfigurations.jsPromiseTransformer)
    }

    fun useDefault() {
        useJvmDefault()
        useJsDefault()
    }

    val includeAnnotation: Property<Boolean>
    val includeRuntime: Property<Boolean>

    val annotationDependency: Property<AnnotationDependencySpec>

    fun annotationDependency(action: Action<in AnnotationDependencySpec>) {
        annotationDependency.set(annotationDependency.get().also(action::execute))
    }

    val runtimeDependency: Property<RuntimeDependencySpec>

    fun runtimeDependency(action: Action<in RuntimeDependencySpec>) {
        runtimeDependency.set(runtimeDependency.get().also(action::execute))
    }

    fun runtimeAsApi() {
        runtimeDependency.get().configurationName.set("api")
    }
}

interface DependencySpec {
    val version: Property<String>
    val configurationName: Property<String>
}

interface AnnotationDependencySpec : DependencySpec
interface RuntimeDependencySpec : DependencySpec

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

abstract class TransformerSpec @Inject constructor(private val objects: ObjectFactory) {
    abstract val markAnnotation: Property<MarkAnnotationSpec>

    fun markAnnotation(action: Action<in MarkAnnotationSpec>) {
        markAnnotation.set(markAnnotation.get().also(action::execute))
    }

    fun markAnnotation(block: MarkAnnotationSpec.() -> Unit) {
        markAnnotation(Action(block))
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
        transformFunctionInfo.set(transformFunctionInfo.get().also(action::execute))
    }

    fun transformFunctionInfo(block: FunctionInfoSpec.() -> Unit) {
        transformFunctionInfo(Action(block))
    }

    /**
     * 转化后的返回值类型, 为null时代表与原函数一致。
     */
    abstract val transformReturnType: Property<ClassInfoSpec>

    fun transformReturnType(action: Action<in ClassInfoSpec>) {
        transformReturnType.set(transformReturnType.get().also(action::execute))
    }

    fun transformReturnType(block: ClassInfoSpec.() -> Unit) {
        transformReturnType(Action(block))
    }

    /**
     * 转化后的返回值类型中，是否存在需要与原本返回值类型一致的泛型。
     */
    abstract val transformReturnTypeGeneric: Property<Boolean>

    /**
     * 函数生成后，需要在原函数上追加的注解信息。
     *
     * 例如追加个 `@kotlin.jvm.JvmSynthetic` 之类的。
     */
    abstract val originFunctionIncludeAnnotations: DomainObjectSet<IncludeAnnotationSpec>

    private fun newIncludeAnnotationSpec(): IncludeAnnotationSpec =
        objects.newInstance(IncludeAnnotationSpec::class.java)

    fun addOriginFunctionIncludeAnnotation(action: Action<IncludeAnnotationSpec>) {
        originFunctionIncludeAnnotations.add(
            newIncludeAnnotationSpec().also(action::execute)
        )
    }

    abstract val syntheticFunctionIncludeAnnotations: DomainObjectSet<IncludeAnnotationSpec>

    fun addSyntheticFunctionIncludeAnnotation(action: Action<IncludeAnnotationSpec>) {
        syntheticFunctionIncludeAnnotations.add(
            newIncludeAnnotationSpec().also(action::execute)
        )
    }

    /**
     * 是否复制源函数上的注解到新的函数上。
     * 如果生成的是属性类型，则表示是否复制到 `getter` 上。
     */
    abstract val copyAnnotationsToSyntheticFunction: Property<Boolean>

    /**
     * 复制原函数上注解时需要排除掉的注解。
     */
    abstract val copyAnnotationExcludes: DomainObjectSet<ClassInfoSpec>

    fun addCopyAnnotationExclude(action: Action<in ClassInfoSpec>) {
        copyAnnotationExcludes.add(objects.newInstance(ClassInfoSpec::class.java).also(action::execute))
    }

    /**
     * 如果是生成属性的话，是否复制源函数上的注解到新的属性上
     *
     * @since 0.9.0
     */
    abstract val copyAnnotationsToSyntheticProperty: Property<Boolean> // = false

    /**
     * Configures the current specification using a [Transformer].
     *
     * @param transformer see [Transformer] or the constants in [SuspendTransformConfigurations],
     * e.g. [SuspendTransformConfigurations.jvmBlockingTransformer].
     */
    fun from(transformer: Transformer) {
        markAnnotation { from(transformer.markAnnotation) }
        transformFunctionInfo { from(transformer.transformFunctionInfo) }
        transformer.transformReturnType?.also { transformReturnType { from(it) } }
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
interface MarkAnnotationSpec {
    /**
     * 注解类信息
     */
    val classInfo: Property<ClassInfoSpec>

    fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.get().also(action::execute))
    }

    fun classInfo(block: ClassInfoSpec.() -> Unit) {
        classInfo(Action(block))
    }

    /**
     * 用于标记生成函数需要使用的基础函数名的注解属性名。
     */
    val baseNameProperty: Property<String> // = "baseName",

    /**
     * 用于标记生成函数需要使用的基础函数名之后的后缀的注解属性名。
     */
    val suffixProperty: Property<String> // = "suffix",

    /**
     * 用于标记生成函数是否需要转化为 property 类型的注解属性名。
     */
    val asPropertyProperty: Property<String> // = "asProperty",

    /**
     * 当 [suffixProperty] 不存在时使用的默认后缀
     */
    val defaultSuffix: Property<String> // = ""

    /**
     * 当 [asPropertyProperty] 不存在时使用的默认值
     */
    val defaultAsProperty: Property<Boolean> // = false,

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
 * @see ClassInfo
 */
interface ClassInfoSpec {
    val packageName: Property<String>
    val className: Property<String>
    val local: Property<Boolean>
    val nullable: Property<Boolean>

    fun from(classInfo: ClassInfo) {
        packageName.set(classInfo.packageName)
        className.set(classInfo.className)
        local.set(classInfo.local)
        nullable.set(classInfo.nullable)
    }
}

interface FunctionInfoSpec {
    val packageName: Property<String>
    val functionName: Property<String>

    fun from(functionInfo: FunctionInfo) {
        packageName.set(functionInfo.packageName)
        functionName.set(functionInfo.functionName)
    }
}

interface IncludeAnnotationSpec {
    val classInfo: Property<ClassInfoSpec>

    fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.get().also(action::execute))
    }

    fun classInfo(block: ClassInfoSpec.() -> Unit) {
        classInfo(Action(block))
    }

    val repeatable: Property<Boolean>

    val includeProperty: Property<Boolean>

    fun from(includeAnnotation: IncludeAnnotation) {
        classInfo {
            from(includeAnnotation.classInfo)
        }
        repeatable.set(includeAnnotation.repeatable)
        includeProperty.set(includeAnnotation.includeProperty)
    }
}
