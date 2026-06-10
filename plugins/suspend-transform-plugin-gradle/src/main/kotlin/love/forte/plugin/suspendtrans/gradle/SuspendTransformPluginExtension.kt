/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.configuration.*
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jsPromiseTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmAsyncTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmBlockingTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmReactiveTransformer
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.util.*
import javax.inject.Inject

/**
 * Marks the suspend-transform Gradle extension DSL scope.
 *
 * @since 0.12.0
 */
@DslMarker
annotation class SuspendTransformPluginExtensionSpecDslMarker

/**
 * Base marker interface for suspend-transform Gradle DSL specifications.
 *
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
    /**
     * Configures the nested [ClassInfoSpec] with a Gradle [Action].
     */
    fun classInfo(action: Action<in ClassInfoSpec>)

    /**
     * Configures the nested [ClassInfoSpec] with the Kotlin DSL.
     */
    fun classInfo(action: ClassInfoSpec.() -> Unit)
}

/**
 * Factory for DSL specification objects that can be configured before being
 * attached to the extension.
 *
 * @since 0.12.0
 */
@Suppress("unused")
interface SuspendTransformPluginExtensionSpecFactory {
    /**
     * Creates a [ClassInfoSpec] and configures it with a Gradle [Action].
     */
    fun createClassInfo(action: Action<in ClassInfoSpec>): ClassInfoSpec

    /**
     * Creates a [ClassInfoSpec] and configures it with the Kotlin DSL.
     */
    fun createClassInfo(action: ClassInfoSpec.() -> Unit): ClassInfoSpec =
        createClassInfo(Action(action))

    /**
     * Creates an empty [ClassInfoSpec].
     */
    fun createClassInfo(): ClassInfoSpec =
        createClassInfo { }

    /**
     * Creates a [MarkAnnotationSpec] and configures it with a Gradle [Action].
     */
    fun createMarkAnnotation(action: Action<in MarkAnnotationSpec>): MarkAnnotationSpec

    /**
     * Creates a [MarkAnnotationSpec] and configures it with the Kotlin DSL.
     */
    fun createMarkAnnotation(action: MarkAnnotationSpec.() -> Unit): MarkAnnotationSpec =
        createMarkAnnotation(Action(action))

    /**
     * Creates an empty [MarkAnnotationSpec].
     */
    fun createMarkAnnotation(): MarkAnnotationSpec =
        createMarkAnnotation { }

    /**
     * Creates a [FunctionInfoSpec] and configures it with a Gradle [Action].
     */
    fun createFunctionInfo(action: Action<in FunctionInfoSpec>): FunctionInfoSpec

    /**
     * Creates a [FunctionInfoSpec] and configures it with the Kotlin DSL.
     */
    fun createFunctionInfo(action: FunctionInfoSpec.() -> Unit): FunctionInfoSpec =
        createFunctionInfo(Action(action))

    /**
     * Creates an empty [FunctionInfoSpec].
     */
    fun createFunctionInfo(): FunctionInfoSpec =
        createFunctionInfo { }

    /**
     * Creates an [IncludeAnnotationSpec] and configures it with a Gradle [Action].
     */
    fun createIncludeAnnotation(action: Action<in IncludeAnnotationSpec>): IncludeAnnotationSpec

    /**
     * Creates an [IncludeAnnotationSpec] and configures it with the Kotlin DSL.
     */
    fun createIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit): IncludeAnnotationSpec =
        createIncludeAnnotation(Action(action))

    /**
     * Creates an empty [IncludeAnnotationSpec].
     */
    fun createIncludeAnnotation(): IncludeAnnotationSpec =
        createIncludeAnnotation { }

    /**
     * Creates a [RuntimeDependencySpec] and configures it with a Gradle [Action].
     */
    fun createRuntimeDependency(action: Action<in RuntimeDependencySpec>): RuntimeDependencySpec

    /**
     * Creates a [RuntimeDependencySpec] and configures it with the Kotlin DSL.
     */
    fun createRuntimeDependency(action: RuntimeDependencySpec.() -> Unit): RuntimeDependencySpec =
        createRuntimeDependency(Action(action))

    /**
     * Creates an empty [RuntimeDependencySpec].
     */
    fun createRuntimeDependency(): RuntimeDependencySpec =
        createRuntimeDependency { }

    /**
     * Creates an [AnnotationDependencySpec] and configures it with a Gradle [Action].
     */
    fun createAnnotationDependency(action: Action<in AnnotationDependencySpec>): AnnotationDependencySpec

    /**
     * Creates an [AnnotationDependencySpec] and configures it with the Kotlin DSL.
     */
    fun createAnnotationDependency(action: AnnotationDependencySpec.() -> Unit): AnnotationDependencySpec =
        createAnnotationDependency(Action(action))

    /**
     * Creates an empty [AnnotationDependencySpec].
     */
    fun createAnnotationDependency(): AnnotationDependencySpec =
        createAnnotationDependency { }

    /**
     * Creates a [FunctionInfoSpec] for a transform function with a Gradle [Action].
     */
    fun createTransformFunctionInfo(action: Action<in FunctionInfoSpec>): FunctionInfoSpec

    /**
     * Creates a [FunctionInfoSpec] for a transform function with the Kotlin DSL.
     */
    fun createTransformFunctionInfo(action: FunctionInfoSpec.() -> Unit): FunctionInfoSpec =
        createTransformFunctionInfo(Action(action))

    /**
     * Creates an empty [FunctionInfoSpec] for a transform function.
     */
    fun createTransformFunctionInfo(): FunctionInfoSpec =
        createTransformFunctionInfo { }

    /**
     * Creates a [TransformerSpec] and configures it with a Gradle [Action].
     */
    fun createTransformer(action: Action<in TransformerSpec>): TransformerSpec

    /**
     * Creates a [TransformerSpec] and configures it with the Kotlin DSL.
     */
    fun createTransformer(action: TransformerSpec.() -> Unit): TransformerSpec =
        createTransformer(Action(action))

    /**
     * Creates an empty [TransformerSpec].
     */
    fun createTransformer(): TransformerSpec =
        createTransformer { }
}

/**
 * Provides access to the shared DSL specification factory.
 *
 * @since 0.12.0
 */
interface SuspendTransformPluginExtensionSpecFactoryAware {
    /**
     * Factory used to create detached DSL specification objects.
     */
    val factory: SuspendTransformPluginExtensionSpecFactory
}

/**
 * Default [SuspendTransformPluginExtensionSpecFactory] backed by Gradle's [ObjectFactory].
 */
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
 * Container for transformer specifications grouped by target platform.
 *
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

    /**
     * Transformer specifications keyed by target platform.
     */
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


    /**
     * Returns the mutable transformer list property for [platform], creating it when necessary.
     */
    private fun getTransformersInternal(platform: TargetPlatform): ListProperty<TransformerSpec> {
        return containers.computeIfAbsent(platform) { objects.listProperty(TransformerSpec::class.java) }
    }

    /**
     * Creates a [TransformerSpec] without adding it to this container.
     */
    fun createTransformer(action: Action<in TransformerSpec>): TransformerSpec {
        return objects.newInstance<TransformerSpec>().also(action::execute)
    }

    /**
     * Creates a [TransformerSpec] without adding it to this container.
     */
    fun createTransformer(action: TransformerSpec.() -> Unit): TransformerSpec {
        return factory.createTransformer(action)
    }

    /**
     * Adds a transformer specification for the given [platform].
     */
    fun add(platform: TargetPlatform, action: Action<in TransformerSpec>) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(createTransformer(action))
    }

    /**
     * Adds a transformer specification for the given [platform].
     */
    fun add(platform: TargetPlatform, action: TransformerSpec.() -> Unit) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(createTransformer(action))
    }

    /**
     * Adds an existing [TransformerSpec] for the given [platform].
     */
    fun add(platform: TargetPlatform, transformer: TransformerSpec) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(transformer)
    }

    /**
     * Adds a provider of [TransformerSpec] for the given [platform].
     */
    fun add(platform: TargetPlatform, transformer: Provider<TransformerSpec>) {
        val transformerSpecs = getTransformersInternal(platform)
        transformerSpecs.add(transformer)
    }

    /**
     * Adds a configuration [Transformer] for the given [platform].
     */
    fun add(platform: TargetPlatform, transformer: Transformer) {
        add(platform) {
            from(transformer)
        }
    }

    /**
     * Adds a JVM transformer.
     */
    fun addJvm(transformer: TransformerSpec) = add(TargetPlatform.JVM, transformer)

    /**
     * Adds a JVM transformer from an existing [Transformer].
     */
    fun addJvm(transformer: Transformer) = addJvm { from(transformer) }

    /**
     * Adds a provider of a JVM transformer.
     */
    fun addJvm(transformer: Provider<TransformerSpec>) = add(TargetPlatform.JVM, transformer)

    /**
     * Adds a JVM transformer configured with a Gradle [Action].
     */
    fun addJvm(action: Action<in TransformerSpec>) = add(TargetPlatform.JVM, action)

    /**
     * Adds a JVM transformer configured with the Kotlin DSL.
     */
    fun addJvm(action: TransformerSpec.() -> Unit) = add(TargetPlatform.JVM, action)

    /**
     * Adds a JS transformer.
     */
    fun addJs(transformer: TransformerSpec) = add(TargetPlatform.JS, transformer)

    /**
     * Adds a JS transformer from an existing [Transformer].
     */
    fun addJs(transformer: Transformer) = addJs { from(transformer) }

    /**
     * Adds a provider of a JS transformer.
     */
    fun addJs(transformer: Provider<TransformerSpec>) = add(TargetPlatform.JS, transformer)

    /**
     * Adds a JS transformer configured with a Gradle [Action].
     */
    fun addJs(action: Action<in TransformerSpec>) = add(TargetPlatform.JS, action)

    /**
     * Adds a JS transformer configured with the Kotlin DSL.
     */
    fun addJs(action: TransformerSpec.() -> Unit) = add(TargetPlatform.JS, action)

    /**
     * Adds a Kotlin/Native transformer.
     */
    fun addNative(transformer: TransformerSpec) = add(TargetPlatform.NATIVE, transformer)

    /**
     * Adds a Kotlin/Native transformer from an existing [Transformer].
     */
    fun addNative(transformer: Transformer) = addNative { from(transformer) }

    /**
     * Adds a provider of a Kotlin/Native transformer.
     */
    fun addNative(transformer: Provider<TransformerSpec>) = add(TargetPlatform.NATIVE, transformer)

    /**
     * Adds a Kotlin/Native transformer configured with a Gradle [Action].
     */
    fun addNative(action: Action<in TransformerSpec>) = add(TargetPlatform.NATIVE, action)

    /**
     * Adds a Kotlin/Native transformer configured with the Kotlin DSL.
     */
    fun addNative(action: TransformerSpec.() -> Unit) = add(TargetPlatform.NATIVE, action)

    /**
     * Adds a Kotlin/Wasm transformer.
     */
    fun addWasm(transformer: TransformerSpec) = add(TargetPlatform.WASM, transformer)

    /**
     * Adds a Kotlin/Wasm transformer from an existing [Transformer].
     */
    fun addWasm(transformer: Transformer) = addWasm { from(transformer) }

    /**
     * Adds a provider of a Kotlin/Wasm transformer.
     */
    fun addWasm(transformer: Provider<TransformerSpec>) = add(TargetPlatform.WASM, transformer)

    /**
     * Adds a Kotlin/Wasm transformer configured with a Gradle [Action].
     */
    fun addWasm(action: Action<in TransformerSpec>) = add(TargetPlatform.WASM, action)

    /**
     * Adds a Kotlin/Wasm transformer configured with the Kotlin DSL.
     */
    fun addWasm(action: TransformerSpec.() -> Unit) = add(TargetPlatform.WASM, action)

    /**
     * Adds a common transformer.
     */
    fun addCommon(transformer: TransformerSpec) = add(TargetPlatform.COMMON, transformer)

    /**
     * Adds a common transformer from an existing [Transformer].
     */
    fun addCommon(transformer: Transformer) = addCommon { from(transformer) }

    /**
     * Adds a provider of a common transformer.
     */
    fun addCommon(transformer: Provider<TransformerSpec>) = add(TargetPlatform.COMMON, transformer)

    /**
     * Adds a common transformer configured with the Kotlin DSL.
     */
    fun addCommon(action: TransformerSpec.() -> Unit) = add(TargetPlatform.COMMON, action)


    // JVM defaults

    /**
     * Adds the default JVM blocking transformer.
     */
    fun addJvmBlocking() {
        addJvm(jvmBlockingTransformer)
    }

    /**
     * Adds the default JVM blocking transformer and customizes it with a Gradle [Action].
     */
    fun addJvmBlocking(action: Action<in TransformerSpec>) {
        addJvm {
            from(jvmBlockingTransformer)
            action.execute(this)
        }
    }

    /**
     * Adds the default JVM blocking transformer and customizes it with the Kotlin DSL.
     */
    fun addJvmBlocking(action: TransformerSpec.() -> Unit) {
        addJvm {
            from(jvmBlockingTransformer)
            action()
        }
    }

    /**
     * Adds the default JVM async transformer.
     */
    fun addJvmAsync() {
        addJvm(jvmAsyncTransformer)
    }

    /**
     * Adds the default JVM async transformer and customizes it with a Gradle [Action].
     */
    fun addJvmAsync(action: Action<in TransformerSpec>) {
        addJvm {
            from(jvmAsyncTransformer)
            action.execute(this)
        }
    }

    /**
     * Adds the default JVM async transformer and customizes it with the Kotlin DSL.
     */
    fun addJvmAsync(action: TransformerSpec.() -> Unit) {
        addJvm {
            from(jvmAsyncTransformer)
            action()
        }
    }

    /**
     * Adds the default JVM Reactive Streams transformer.
     */
    fun addJvmReactive() {
        addJvm(jvmReactiveTransformer)
    }

    /**
     * Adds the default JVM Reactive Streams transformer and customizes it with a Gradle [Action].
     */
    fun addJvmReactive(action: Action<in TransformerSpec>) {
        addJvm {
            from(jvmReactiveTransformer)
            action.execute(this)
        }
    }

    /**
     * Adds the default JVM Reactive Streams transformer and customizes it with the Kotlin DSL.
     */
    fun addJvmReactive(action: TransformerSpec.() -> Unit) {
        addJvm {
            from(jvmReactiveTransformer)
            action()
        }
    }

    // JS defaults

    /**
     * Adds the default JS Promise transformer.
     */
    fun addJsPromise() {
        addJs(jsPromiseTransformer)
    }

    /**
     * Adds the default JS Promise transformer and customizes it with the Kotlin DSL.
     */
    fun addJsPromise(action: TransformerSpec.() -> Unit) {
        addJs {
            from(jsPromiseTransformer)
            action()
        }
    }

    /**
     * Adds all default JVM transformers.
     */
    fun useJvmDefault() {
        addJvmBlocking()
        addJvmAsync()
        addJvmReactive()
    }

    /**
     * Adds all default JS transformers.
     */
    fun useJsDefault() {
        addJsPromise()
    }

    /**
     * Adds the default transformers for supported platforms.
     */
    fun useDefault() {
        useJvmDefault()
        useJsDefault()
    }
}

/**
 * Main Gradle extension for configuring the suspend-transform plugin.
 *
 * @since 0.12.0
 */
@Suppress("unused")
abstract class SuspendTransformPluginExtension
@Inject constructor(objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    /**
     * Whether the plugin is enabled.
     *
     * Default is `true`.
     */
    abstract val enabled: Property<Boolean>

    /**
     * Transformer definitions grouped by target platform.
     */
    val transformers: TransformersContainer = objects.newInstance()

    /**
     * Configures [transformers] with a Gradle [Action].
     */
    fun transformers(action: Action<in TransformersContainer>) {
        action.execute(transformers)
    }

    /**
     * Configures [transformers] with the Kotlin DSL.
     */
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
     * Dependency configuration for the annotation artifact.
     *
     * Default is `compileOnly` with [SuspendTransPluginConstants.ANNOTATION_VERSION].
     */
    abstract val annotationDependency: Property<AnnotationDependencySpec>

    /**
     * Configures [annotationDependency] with a Gradle [Action].
     */
    fun annotationDependency(action: Action<in AnnotationDependencySpec>) {
        annotationDependency.set(annotationDependency.get().also(action::execute))
    }

    /**
     * Configures [annotationDependency] with the Kotlin DSL.
     */
    fun annotationDependency(action: AnnotationDependencySpec.() -> Unit) {
        annotationDependency.set(annotationDependency.get().also(action))
    }

    /**
     * Dependency configuration for the runtime artifact.
     *
     * Default is `implementation` with [SuspendTransPluginConstants.RUNTIME_VERSION].
     */
    abstract val runtimeDependency: Property<RuntimeDependencySpec>

    /**
     * Configures [runtimeDependency] with a Gradle [Action].
     */
    fun runtimeDependency(action: Action<in RuntimeDependencySpec>) {
        runtimeDependency.set(runtimeDependency.get().also(action::execute))
    }

    /**
     * Configures [runtimeDependency] with the Kotlin DSL.
     */
    fun runtimeDependency(action: RuntimeDependencySpec.() -> Unit) {
        runtimeDependency.set(runtimeDependency.get().also(action))
    }

    /**
     * Uses the `api` configuration for the runtime dependency.
     */
    fun runtimeAsApi() {
        runtimeDependency.get().configurationName.set("api")
    }
}

/**
 * Intermediate entry used while collecting transformer providers into a configuration map.
 */
internal data class TransformerEntry(
    /**
     * Target platform for [transformers].
     */
    val targetPlatform: TargetPlatform,

    /**
     * Transformers configured for [targetPlatform].
     */
    val transformers: List<Transformer>
)

/**
 * Converts the Gradle extension into a lazy compiler plugin configuration provider.
 */
@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun SuspendTransformPluginExtension.toConfigurationProvider(project: Project, objects: ObjectFactory): Provider<SuspendTransformConfiguration> {
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

        project.logger.trace("Transformers: {}", transformersMap)

        // The map may be empty, but each stored transformer list is non-empty.
        // Consumers only need to check whether the transformers map itself is empty.
        SuspendTransformConfiguration(
            transformers = transformersMap
        )
    }

}

/**
 * Base dependency specification used by annotation and runtime dependency settings.
 *
 * @since 0.12.0
 */
sealed interface DependencySpec : SuspendTransformPluginExtensionSpec {
    /**
     * Dependency version.
     */
    val version: Property<String>

    /**
     * Gradle configuration name used to add the dependency.
     */
    val configurationName: Property<String>
}

/**
 * Dependency specification for the annotation artifact.
 *
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
 * Dependency specification for the runtime artifact.
 *
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

/**
 * Applies default values to the main extension properties.
 */
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
 * DSL specification for one suspend transform rule.
 *
 * @since 0.12.0
 */
@Suppress("unused")
abstract class TransformerSpec
@Inject constructor(private val objects: ObjectFactory) : SuspendTransformPluginExtensionSpec {
    /**
     * Annotation that marks suspend functions to be transformed.
     *
     * @see Transformer.markAnnotation
     */
    abstract val markAnnotation: Property<MarkAnnotationSpec>

    /**
     * Configures [markAnnotation] with a Gradle [Action].
     */
    fun markAnnotation(action: Action<in MarkAnnotationSpec>) {
        val old = markAnnotation.getOrElse(objects.newInstance<MarkAnnotationSpec>())
        markAnnotation.set(old.also(action::execute))
    }

    /**
     * Configures [markAnnotation] with the Kotlin DSL.
     */
    fun markAnnotation(action: MarkAnnotationSpec.() -> Unit) {
        val old = markAnnotation.getOrElse(objects.newInstance<MarkAnnotationSpec>())
        markAnnotation.set(old.also(action))
    }

    /**
     * Function used to transform the suspend call.
     *
     * The function must have the following shape:
     *
     * ```kotlin
     * fun <T> <fun-name>(block: suspend () -> T[, scope: CoroutineScope = ...]): T {
     *     // ...
     * }
     * ```
     *
     * The function may have a second parameter of type [kotlinx.coroutines.CoroutineScope].
     * When that parameter exists and the transformed function's containing type
     * implements [kotlinx.coroutines.CoroutineScope], the generated function passes
     * the receiver as the scope argument, for example:
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

    /**
     * Configures [transformFunctionInfo] with a Gradle [Action].
     */
    fun transformFunctionInfo(action: Action<in FunctionInfoSpec>) {
        transformFunctionInfo.set(
            transformFunctionInfo.getOrElse(
                objects.newInstance()
            ).also(action::execute)
        )
    }

    /**
     * Configures [transformFunctionInfo] with the Kotlin DSL.
     */
    fun transformFunctionInfo(action: FunctionInfoSpec.() -> Unit) {
        transformFunctionInfo.set(
            transformFunctionInfo.getOrElse(
                objects.newInstance()
            ).also(action)
        )
    }

    /**
     * Transformed return type. When absent, the generated function uses the
     * original suspend function return type.
     *
     * Will be used when [transformReturnTypeGeneric] is `true`.
     */
    abstract val transformReturnType: Property<ClassInfoSpec>

    /**
     * Configures [transformReturnType] with a Gradle [Action].
     */
    fun transformReturnType(action: Action<in ClassInfoSpec>) {
        transformReturnType.set(
            transformReturnType.getOrElse(
                objects.newInstance()
            ).also(action::execute)
        )
    }

    /**
     * Configures [transformReturnType] with the Kotlin DSL.
     */
    fun transformReturnType(action: ClassInfoSpec.() -> Unit) {
        transformReturnType.set(
            transformReturnType.getOrElse(
                objects.newInstance()
            ).also(action)
        )
    }

    /**
     * Whether the transformed return type has a generic argument copied from the
     * original return type.
     *
     * This refers to nested generic arguments, such as `T` in
     * `CompletableFuture<T>`. If the generated function directly returns `T`,
     * this does not need to be `true`.
     *
     * Default value is `false`.
     */
    val transformReturnTypeGeneric: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    /**
     * Sets [transformReturnTypeGeneric] to `true`.
     */
    fun transformReturnTypeGeneric() {
        transformReturnTypeGeneric.set(true)
    }

    /**
     * Nullability mode for the generic argument copied from the original return type.
     *
     * This is used only when [transformReturnType] is configured and
     * [transformReturnTypeGeneric] is `true`. It affects the type argument placed
     * inside the transformed return type, for example `CompletableFuture<T>`, and
     * does not change the source function's type parameters or bounds.
     *
     * [TransformReturnTypeGenericMode.NON_NULL] can produce definitely-not-null
     * type arguments such as `T & Any` for a source return type like `T : Foo?`.
     * This is mainly intended for wrappers whose element type cannot be `null`,
     * such as Reactive Streams-style APIs that model absence as an empty result.
     *
     * This setting only changes the generated type. The configured transform
     * function must still provide matching runtime behavior when the wrapper
     * cannot emit `null`.
     *
     * Default value is [TransformReturnTypeGenericMode.NORMAL].
     *
     * @see transformReturnTypeGenericModeNonNull
     * @see transformReturnTypeGenericModeNullable
     *
     * @since 0.14.0
     */
    val transformReturnTypeGenericMode: Property<TransformReturnTypeGenericMode> =
        objects.property(TransformReturnTypeGenericMode::class.java)
            .convention(TransformReturnTypeGenericMode.NORMAL)

    /**
     * Set [transformReturnTypeGenericMode] to [TransformReturnTypeGenericMode.NON_NULL].
     */
    fun transformReturnTypeGenericModeNonNull() {
        transformReturnTypeGenericMode.set(TransformReturnTypeGenericMode.NON_NULL)
    }

    /**
     * Set [transformReturnTypeGenericMode] to [TransformReturnTypeGenericMode.NULLABLE].
     */
    fun transformReturnTypeGenericModeNullable() {
        transformReturnTypeGenericMode.set(TransformReturnTypeGenericMode.NULLABLE)
    }

    /**
     * Annotations added to the original function after the transformed function is generated.
     *
     * For example, this can be used to add `@kotlin.jvm.JvmSynthetic`.
     */
    abstract val originFunctionIncludeAnnotations: DomainObjectSet<IncludeAnnotationSpec>

    private fun newIncludeAnnotationSpec(): IncludeAnnotationSpec =
        objects.newInstance()

    /**
     * Creates an [IncludeAnnotationSpec] without adding it to any annotation set.
     */
    fun createIncludeAnnotation(action: Action<in IncludeAnnotationSpec>): IncludeAnnotationSpec {
        return newIncludeAnnotationSpec().also(action::execute)
    }

    /**
     * Creates an [IncludeAnnotationSpec] without adding it to any annotation set.
     */
    fun createIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit): IncludeAnnotationSpec {
        return newIncludeAnnotationSpec().also(action)
    }

    /**
     * Adds an annotation to the original function.
     */
    fun addOriginFunctionIncludeAnnotation(action: Action<in IncludeAnnotationSpec>) {
        originFunctionIncludeAnnotations.add(
            newIncludeAnnotationSpec().also(action::execute)
        )
    }

    /**
     * Adds an annotation to the original function.
     */
    fun addOriginFunctionIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit) {
        originFunctionIncludeAnnotations.add(
            newIncludeAnnotationSpec().also(action)
        )
    }

    /**
     * Annotations added to the generated synthetic function.
     */
    abstract val syntheticFunctionIncludeAnnotations: DomainObjectSet<IncludeAnnotationSpec>

    /**
     * Adds an annotation to the generated synthetic function.
     */
    fun addSyntheticFunctionIncludeAnnotation(action: Action<in IncludeAnnotationSpec>) {
        syntheticFunctionIncludeAnnotations.add(createIncludeAnnotation(action))
    }

    /**
     * Adds an annotation to the generated synthetic function.
     */
    fun addSyntheticFunctionIncludeAnnotation(action: IncludeAnnotationSpec.() -> Unit) {
        syntheticFunctionIncludeAnnotations.add(createIncludeAnnotation(action))
    }

    /**
     * Whether annotations from the source function are copied to the generated function.
     *
     * If the generated member is a property, this controls whether the annotations
     * are copied to the getter.
     *
     * Default value is `false`.
     */
    val copyAnnotationsToSyntheticFunction: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    /**
     * Sets [copyAnnotationsToSyntheticFunction] to `true`.
     */
    fun copyAnnotationsToSyntheticFunction() {
        copyAnnotationsToSyntheticFunction.set(true)
    }

    /**
     * Annotations excluded when copying annotations from the source function.
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
     * Whether annotations from the source function are copied to the generated
     * property when the generated member is a property.
     *
     * @since 0.9.0
     */
    val copyAnnotationsToSyntheticProperty: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    /**
     * Sets [copyAnnotationsToSyntheticProperty] to `true`.
     */
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
        transformReturnTypeGenericMode.set(transformer.transformReturnTypeGenericMode)
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
 * DSL specification for [MarkAnnotation].
 *
 * @see MarkAnnotation
 *
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

    /**
     * Configures [classInfo] with a Gradle [Action].
     */
    override fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action::execute))
    }

    /**
     * Configures [classInfo] with the Kotlin DSL.
     */
    override fun classInfo(action: ClassInfoSpec.() -> Unit) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action))
    }

    /**
     * Annotation property name used to read the base name for the generated function.
     *
     * Default value is `"baseName"`
     */
    val baseNameProperty: Property<String> =
        objects.property(String::class.java).convention("baseName")

    /**
     * Annotation property name used to read the suffix appended to the generated function name.
     *
     * Default value is `"suffix"`
     */
    val suffixProperty: Property<String> =
        objects.property(String::class.java).convention("suffix")

    /**
     * Annotation property name used to read whether the generated member should be a property.
     *
     * Default value is `"asProperty"`
     */
    val asPropertyProperty: Property<String> =
        objects.property(String::class.java).convention("asProperty")

    /**
     * Default suffix used when [suffixProperty] is absent from the annotation.
     *
     * Default value is `""`
     */
    val defaultSuffix: Property<String> =
        objects.property(String::class.java).convention("")

    /**
     * Default property-generation flag used when [asPropertyProperty] is absent from the annotation.
     */
    val defaultAsProperty: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    /**
     * Optional property mapping for platform name annotations such as `@JvmName`.
     *
     * @since 0.13.0
     */
    abstract val markNameProperty: Property<MarkNamePropertySpec>

    /**
     * Configures [markNameProperty] with a Gradle [Action].
     */
    fun markNameProperty(action: Action<in MarkNamePropertySpec>) {
        markNameProperty.set(markNameProperty.getOrElse(objects.newInstance<MarkNamePropertySpec>()).also(action::execute))
    }

    /**
     * Configures [markNameProperty] with the Kotlin DSL.
     */
    fun markNameProperty(action: MarkNamePropertySpec.() -> Unit) {
        markNameProperty.set(markNameProperty.getOrElse(objects.newInstance<MarkNamePropertySpec>()).also(action))
    }

    /**
     * Configures this specification from an existing [MarkAnnotation].
     */
    fun from(markAnnotation: MarkAnnotation) {
        classInfo {
            from(markAnnotation.classInfo)
        }
        baseNameProperty.set(markAnnotation.baseNameProperty)
        suffixProperty.set(markAnnotation.suffixProperty)
        asPropertyProperty.set(markAnnotation.asPropertyProperty)
        defaultSuffix.set(markAnnotation.defaultSuffix)
        defaultAsProperty.set(markAnnotation.defaultAsProperty)
        // from markName, since 0.13.0
        markAnnotation.markNameProperty?.also { markNameProperty ->
            markNameProperty {
                from(markNameProperty)

            }
        }

    }
}

/**
 * Specification interface for class name information.
 * Used to define properties for package name and class name.
 *
 * @since 0.13.0
 */
interface ClassNameSpec : SuspendTransformPluginExtensionSpec {
    /**
     * Package name for the configured class.
     */
    val packageName: Property<String>

    /**
     * Simple or qualified class name within [packageName].
     */
    val className: Property<String>

    /**
     * Configures this specification from an existing [ClassInfo].
     */
    fun from(classInfo: ClassInfo) {
        packageName.set(classInfo.packageName)
        className.set(classInfo.className)
    }
}

/**
 * DSL specification for [ClassInfo].
 *
 * @see ClassInfo
 * @since 0.12.0
 */
interface ClassInfoSpec : SuspendTransformPluginExtensionSpec, ClassNameSpec {
    /**
     * Package name for the configured class.
     */
    override val packageName: Property<String>

    /**
     * Simple or qualified class name within [packageName].
     */
    override val className: Property<String>

    /**
     * Whether the class is local to the generated declaration context.
     *
     * Default value is `false`
     */
    val local: Property<Boolean>

    /**
     * Whether this class type should be treated as nullable.
     *
     * Default value is `false`
     */
    val nullable: Property<Boolean>

    /**
     * Configures this specification from an existing [ClassInfo].
     */
    override fun from(classInfo: ClassInfo) {
        packageName.set(classInfo.packageName)
        className.set(classInfo.className)
        local.set(classInfo.local)
        nullable.set(classInfo.nullable)
    }
}

/**
 * DSL specification for mark-name annotation mapping.
 *
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

    /**
     * The name marker annotation.
     */
    fun annotation(action: Action<in MarkNameAnnotationSpec>) {
        annotation.set(annotation.getOrElse(objects.newInstance<MarkNameAnnotationSpec>()).also(action::execute))
    }

    /**
     * The name marker annotation.
     */
    fun annotation(action: MarkNameAnnotationSpec.() -> Unit) {
        annotation.set(annotation.getOrElse(objects.newInstance<MarkNameAnnotationSpec>()).also(action))
    }

    /**
     * Configures this specification from an existing [MarkNameProperty].
     */
    fun from(markNameProperty: MarkNameProperty) {
        propertyName.set(markNameProperty.propertyName)
        annotation {
            from(markNameProperty)
        }
    }

    companion object {
        /**
         * Default annotation property name used to read the generated platform name.
         */
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
interface MarkNameAnnotationSpec : SuspendTransformPluginExtensionSpec, ClassNameSpec {
    /**
     * Simple class name for the name marker annotation.
     */
    override val className: Property<String>

    /**
     * Package name for the name marker annotation.
     */
    override val packageName: Property<String>

    /**
     * The name's property name,
     * e.g. `name` of `@JsName(name = "...")`, `name` of `@JvmName(name = "...")`, etc.
     */
    val propertyName: Property<String>

    /**
     * Configures this specification from an existing [MarkNameProperty].
     */
    fun from(markNameProperty: MarkNameProperty) {
        from(markNameProperty.annotation)
        propertyName.set(markNameProperty.annotationMarkNamePropertyName)
    }
}

/**
 * Function information specification interface for configuring function-related properties
 *
 * @property packageName The package name property
 * @property functionName The function name property
 * @since 0.12.0
 */
interface FunctionInfoSpec : SuspendTransformPluginExtensionSpec {
    /**
     * Package name containing the function.
     */
    val packageName: Property<String>

    /**
     * Function name.
     */
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
 * DSL specification for an annotation copied or added by the transformer.
 *
 * @since 0.12.0
 */
@Suppress("unused")
abstract class IncludeAnnotationSpec
@Inject constructor(private val objects: ObjectFactory) :
    SuspendTransformPluginExtensionSpec,
    SuspendTransformPluginExtensionClassInfoSpec {
    /**
     * Annotation class information.
     */
    abstract val classInfo: Property<ClassInfoSpec>

    /**
     * Configures [classInfo] with a Gradle [Action].
     */
    override fun classInfo(action: Action<in ClassInfoSpec>) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action::execute))
    }

    /**
     * Configures [classInfo] with the Kotlin DSL.
     */
    override fun classInfo(action: ClassInfoSpec.() -> Unit) {
        classInfo.set(classInfo.getOrElse(objects.newInstance<ClassInfoSpec>()).also(action))
    }

    /**
     * Whether the annotation can be emitted more than once.
     *
     * Default value is `false`
     */
    abstract val repeatable: Property<Boolean>

    /**
     * Whether this annotation should also be included on a generated property.
     *
     * Default value is `false`
     */
    abstract val includeProperty: Property<Boolean>

    /**
     * Configures this specification from an existing [IncludeAnnotation].
     */
    fun from(includeAnnotation: IncludeAnnotation) {
        classInfo {
            from(includeAnnotation.classInfo)
        }
        repeatable.set(includeAnnotation.repeatable)
        includeProperty.set(includeAnnotation.includeProperty)
    }
}

/**
 * Converts a Gradle [TransformerSpec] to a compiler configuration [Transformer].
 */
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
        copyAnnotationsToSyntheticProperty = copyAnnotationsToSyntheticProperty.get(),
        transformReturnTypeGenericMode = transformReturnTypeGenericMode.getOrElse(TransformReturnTypeGenericMode.NORMAL)
    )
}

/**
 * Converts a Gradle [MarkAnnotationSpec] to a compiler configuration [MarkAnnotation].
 */
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

/**
 * Converts a Gradle [MarkNameAnnotationSpec] to a [ClassInfo].
 */
@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun MarkNameAnnotationSpec.toClassInfo(): ClassInfo {
    return ClassInfo(
        packageName = packageName.get(),
        className = className.get(),
    )
}

/**
 * Converts a Gradle [MarkNamePropertySpec] to a compiler configuration [MarkNameProperty].
 */
@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun MarkNamePropertySpec.toMarkNameProperty(): MarkNameProperty {
    val annotation = annotation.get()
    return MarkNameProperty(
        propertyName = propertyName.getOrElse(MarkNamePropertySpec.DEFAULT_PROPERTY_NAME),
        annotation = annotation.toClassInfo(),
        annotationMarkNamePropertyName = annotation.propertyName.get()
    )
}

/**
 * Converts a Gradle [ClassInfoSpec] to a compiler configuration [ClassInfo].
 */
@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun ClassInfoSpec.toClassInfo(): ClassInfo {
    return ClassInfo(
        packageName = packageName.get(),
        className = className.get(),
        local = local.getOrElse(false),
        nullable = nullable.getOrElse(false)
    )
}

/**
 * Converts a Gradle [FunctionInfoSpec] to a compiler configuration [FunctionInfo].
 */
@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun FunctionInfoSpec.toFunctionInfo(): FunctionInfo {
    return FunctionInfo(
        packageName = packageName.get(),
        functionName = functionName.get()
    )
}

/**
 * Converts a Gradle [IncludeAnnotationSpec] to a compiler configuration [IncludeAnnotation].
 */
@OptIn(InternalSuspendTransformConfigurationApi::class)
internal fun IncludeAnnotationSpec.toIncludeAnnotation(): IncludeAnnotation {
    return IncludeAnnotation(
        classInfo = classInfo.get().toClassInfo(),
        repeatable = repeatable.getOrElse(false),
        includeProperty = includeProperty.getOrElse(false)
    )
}

/**
 * Creates a Gradle-managed instance using a reified type parameter.
 */
internal inline fun <reified T : Any> ObjectFactory.newInstance(): T = newInstance(T::class.java)
