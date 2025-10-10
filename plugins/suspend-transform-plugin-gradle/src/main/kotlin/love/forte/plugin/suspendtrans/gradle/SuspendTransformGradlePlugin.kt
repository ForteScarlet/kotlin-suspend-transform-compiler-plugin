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

import love.forte.plugin.suspendtrans.cli.SuspendTransformCliOptions
import love.forte.plugin.suspendtrans.cli.encodeToHex
import love.forte.plugin.suspendtrans.configuration.*
import love.forte.plugin.suspendtrans.gradle.DependencyConfigurationName.*
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*

@Suppress("DEPRECATION")
private typealias DeprecatedClassInfo = love.forte.plugin.suspendtrans.ClassInfo
@Suppress("DEPRECATION")
private typealias DeprecatedFunctionInfo = love.forte.plugin.suspendtrans.FunctionInfo
@Suppress("DEPRECATION")
private typealias DeprecatedIncludeAnnotation = love.forte.plugin.suspendtrans.IncludeAnnotation
@Suppress("DEPRECATION")
private typealias DeprecatedMarkAnnotation = love.forte.plugin.suspendtrans.MarkAnnotation
@Suppress("DEPRECATION")
private typealias DeprecatedSuspendTransformConfiguration = love.forte.plugin.suspendtrans.SuspendTransformConfiguration
@Suppress("DEPRECATION")
private typealias DeprecatedTargetPlatform = love.forte.plugin.suspendtrans.TargetPlatform
@Suppress("DEPRECATION")
private typealias DeprecatedTransformer = love.forte.plugin.suspendtrans.Transformer

/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformGradlePlugin : KotlinCompilerPluginSupportPlugin {
    companion object {
        const val EXTENSION_NAME = "suspendTransform"
        const val PLUGIN_EXTENSION_NAME = "suspendTransformPlugin"
    }

    override fun apply(target: Project) {
        @Suppress("DEPRECATION")
        target.extensions.create(
            EXTENSION_NAME,
            SuspendTransformGradleExtension::class.java
        )

        val createdExtensions = target.extensions.create(
            PLUGIN_EXTENSION_NAME,
            SuspendTransformPluginExtension::class.java,
        )

        createdExtensions.defaults(target.objects, target.providers)

        target.configureDependencies()
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project

        val isApplicable = project.plugins.hasPlugin(SuspendTransformGradlePlugin::class.java)
                && project.configOrNull?.enabled?.get() != false

        project.logger.debug("Is suspend transform plugin applicable for {}: {}", kotlinCompilation, isApplicable)

        return isApplicable
    }

    override fun getCompilerPluginId(): String = SuspendTransPluginConstants.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = SuspendTransPluginConstants.KOTLIN_PLUGIN_GROUP,
            artifactId = SuspendTransPluginConstants.KOTLIN_PLUGIN_NAME,
            version = SuspendTransPluginConstants.KOTLIN_PLUGIN_VERSION
        )
    }


    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val target = kotlinCompilation.target
        val project = target.project

        project.logger.debug("Apply suspend transform plugin to compilation {}, target: {}", kotlinCompilation, target)

        val extension = resolveExtension(project)
        return extension.toSubpluginOptionsProvider(target, project)
    }

    private fun resolveExtension(project: Project): SuspendTransformPluginExtension {
        val extension = project.extensions.getByType(SuspendTransformPluginExtension::class.java)

        @Suppress("DEPRECATION") val oldExtension =
            project.extensions.getByType(SuspendTransformGradleExtension::class.java)
        @Suppress("DEPRECATION")
        if (oldExtension.enabled && oldExtension.transformers.isNotEmpty()) {
            val dontShowWarn =
                project.providers.gradleProperty("love.forte.plugin.suspend-transform.suppressDeprecatedExtensionWarn")
                    .orNull.toBoolean()

            val msg = "WARN: The `love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension` " +
                    "(`suspendTransform { ... }`) is deprecated, \n" +
                    "please use `love.forte.plugin.suspendtrans.gradle.SuspendTransformPluginExtension` " +
                    "(`suspendTransformPlugin { ... }`) instead. \n" +
                    "The SuspendTransformGradleExtension property " +
                    "will currently be aggregated with `SuspendTransformPluginExtension`, " +
                    "but it will soon be deprecated completely. \n" +
                    "Add 'love.forte.plugin.suspend-transform.suppressDeprecatedExtensionWarn=true' " +
                    "to gradle.properties to suppress this warning."

            if (!dontShowWarn) {
                project.logger.warn(msg)
            }

            oldExtension.mergeTo(extension)
        }

        return extension
    }
}

@Suppress("DEPRECATION", "DEPRECATION_ERROR", "TYPEALIAS_EXPANSION_DEPRECATION")
private fun SuspendTransformGradleExtension.mergeTo(extension: SuspendTransformPluginExtension) {
    if (this.enabled) {
        extension.enabled.convention(true)
    }
    if (this.includeAnnotation) {
        extension.includeAnnotation.convention(true)
    }

    // Annotation
    val deprecatedAnnotationVersion = this.annotationDependencyVersion
    // Not the default value
    if (deprecatedAnnotationVersion != SuspendTransPluginConstants.ANNOTATION_VERSION) {
        extension.annotationDependency {
            version.convention(deprecatedAnnotationVersion)
        }
    }
    val deprecatedAnnotationConfigurationName = this.annotationConfigurationName
    if (deprecatedAnnotationConfigurationName != "compileOnly") {
        extension.annotationDependency {
            configurationName.convention(deprecatedAnnotationConfigurationName)
        }
    }

    if (this.includeRuntime) {
        extension.includeRuntime.convention(true)
    }

    // Runtime
    val deprecatedRuntimeVersion = this.runtimeDependencyVersion
    if (deprecatedRuntimeVersion != SuspendTransPluginConstants.RUNTIME_VERSION) {
        extension.runtimeDependency {
            version.convention(deprecatedRuntimeVersion)
        }
    }
    val deprecatedRuntimeConfigurationName = this.runtimeConfigurationName
    if (deprecatedRuntimeConfigurationName != "implementation") {
        extension.runtimeDependency {
            configurationName.convention(deprecatedRuntimeConfigurationName)
        }
    }

    // Transformers
    if (this.transformers.isNotEmpty()) {
        extension.transformers { transformerContainer ->
            for ((deprecatedTarget, deprecatedTransformers) in this.transformers) {
                val target = deprecatedTarget.toTarget()
                deprecatedTransformers.forEach { deprecatedTransformer ->
                    when (deprecatedTransformer) {
                        DeprecatedSuspendTransformConfiguration.jvmBlockingTransformer -> {
                            transformerContainer.add(target, SuspendTransformConfigurations.jvmBlockingTransformer)
                        }

                        DeprecatedSuspendTransformConfiguration.jvmAsyncTransformer -> {
                            transformerContainer.add(target, SuspendTransformConfigurations.jvmAsyncTransformer)
                        }

                        DeprecatedSuspendTransformConfiguration.jsPromiseTransformer -> {
                            transformerContainer.add(target, SuspendTransformConfigurations.jsPromiseTransformer)
                        }

                        else -> transformerContainer.add(target, deprecatedTransformer.toTransformer())
                    }
                }
            }
        }
    }
}

@Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION", "KotlinRedundantDiagnosticSuppress")
private fun DeprecatedTargetPlatform.toTarget(): TargetPlatform {
    return when (this) {
        DeprecatedTargetPlatform.COMMON -> TargetPlatform.COMMON
        DeprecatedTargetPlatform.JVM -> TargetPlatform.JVM
        DeprecatedTargetPlatform.JS -> TargetPlatform.JS
        DeprecatedTargetPlatform.WASM -> TargetPlatform.WASM
        DeprecatedTargetPlatform.NATIVE -> TargetPlatform.NATIVE
    }
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
private fun DeprecatedTransformer.toTransformer(): Transformer {
    return Transformer(
        markAnnotation = this.markAnnotation.toMarkAnnotation(),
        transformFunctionInfo = this.transformFunctionInfo.toFunctionInfo(),
        transformReturnType = this.transformReturnType?.toClassInfo(),
        transformReturnTypeGeneric = this.transformReturnTypeGeneric,
        originFunctionIncludeAnnotations = this.originFunctionIncludeAnnotations.map { it.toIncludeAnnotation() },
        syntheticFunctionIncludeAnnotations = this.syntheticFunctionIncludeAnnotations.map { it.toIncludeAnnotation() },
        copyAnnotationsToSyntheticFunction = this.copyAnnotationsToSyntheticFunction,
        copyAnnotationExcludes = this.copyAnnotationExcludes.map { it.toClassInfo() },
        copyAnnotationsToSyntheticProperty = this.copyAnnotationsToSyntheticProperty
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
private fun DeprecatedMarkAnnotation.toMarkAnnotation(): MarkAnnotation {
    return MarkAnnotation(
        classInfo = this.classInfo.toClassInfo(),
        baseNameProperty = this.baseNameProperty,
        suffixProperty = this.suffixProperty,
        asPropertyProperty = this.asPropertyProperty,
        defaultSuffix = this.defaultSuffix,
        defaultAsProperty = this.defaultAsProperty,
        markNameProperty = null,
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
private fun DeprecatedFunctionInfo.toFunctionInfo(): FunctionInfo {
    return FunctionInfo(
        packageName = this.packageName,
        functionName = this.functionName
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
private fun DeprecatedClassInfo.toClassInfo(): ClassInfo {
    return ClassInfo(
        packageName = this.packageName,
        className = this.className,
        local = this.local,
        nullable = this.nullable
    )
}

@OptIn(InternalSuspendTransformConfigurationApi::class)
@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
private fun DeprecatedIncludeAnnotation.toIncludeAnnotation(): IncludeAnnotation {
    return IncludeAnnotation(
        classInfo = this.classInfo.toClassInfo(),
        repeatable = this.repeatable,
        includeProperty = this.includeProperty
    )
}

private fun SuspendTransformPluginExtension.toSubpluginOptionsProvider(
    target: KotlinTarget,
    project: Project
): Provider<List<SubpluginOption>> {
    return enabled
        .flatMap { isEnabled ->
            if (!isEnabled) {
                project.logger.debug("The suspend transform is disabled for {}.", target)
                return@flatMap project.provider { emptyList() }
            }

            val configurationProvider = toConfigurationProvider(project, project.objects)

            configurationProvider.map { configuration ->
                val transformers = configuration.transformers
                if (transformers.isEmpty()) {
                    project.logger.debug("The suspend transform is enabled but transformers are empty for {}.", target)
                    return@map emptyList()
                }

                val cliConfig = SuspendTransformCliOptions.CLI_CONFIGURATION
                listOf(SubpluginOption(cliConfig.optionName, configuration.encodeToHex()))
            }
        }

}

private fun Project.configureDependencies() {
    fun Project.include(platform: Platform, conf: SuspendTransformPluginExtension) {
        if (!conf.enabled.get()) {
            logger.info(
                "The `SuspendTransformPluginExtension.enable` in project {} for platform {} is `false`, skip config.",
                this,
                platform
            )
            return
        }

        if (conf.includeAnnotation.get()) {
            // val notation = getDependencyNotation(
            val notation = getDependencyNotation(
                SuspendTransPluginConstants.ANNOTATION_GROUP,
                SuspendTransPluginConstants.ANNOTATION_NAME,
                platform,
                conf.annotationDependency.flatMap { it.version }
            )

            var configName = conf.annotationDependency.get().configurationName.get()
            if (configName == "compileOnly" && platform != Platform.JVM) {
                configName = "implementation"
            }

            dependencies.add(configName, notation)
            dependencies.add("testImplementation", notation)
        }

        if (conf.includeRuntime.get()) {
            val notation = getDependencyNotation(
                SuspendTransPluginConstants.RUNTIME_GROUP,
                SuspendTransPluginConstants.RUNTIME_NAME,
                platform,
                conf.runtimeDependency.flatMap { it.version }
            )
            var configName = conf.runtimeDependency.get().configurationName.get()
            if (configName == "compileOnly" && platform != Platform.JVM) {
                // JS, native 似乎不支持 compileOnly，因此如果不是JVM，更换为 implementation
                configName = "implementation"
            }

            dependencies.add(configName, notation)
            dependencies.add("testImplementation", notation)
        }
    }

    withPluginWhenEvaluatedConf("kotlin") { conf ->
        include(Platform.JVM, conf)
    }
    withPluginWhenEvaluatedConf("org.jetbrains.kotlin.js") { conf ->
        include(Platform.JS, conf)
    }
    withPluginWhenEvaluatedConf("kotlin-multiplatform") { conf ->
        configureMultiplatformDependency(conf)
    }
//    withPluginWhenEvaluatedConf("kotlin-multiplatform") {
//
//    }
}

// Note "afterEvaluate" does nothing when the project is already in executed state, so we need
// a special check for this case
fun <T> Project.whenEvaluated(fn: Project.() -> T) {
    if (state.executed) {
        fn()
    } else {
        afterEvaluate { fn() }
    }
}

fun Project.withPluginWhenEvaluated(plugin: String, fn: Project.() -> Unit) {
    pluginManager.withPlugin(plugin) { whenEvaluated(fn) }
}

fun Project.withPluginWhenEvaluatedConf(
    plugin: String,
    fn: Project.(conf: SuspendTransformPluginExtension) -> Unit
) {
    withPluginWhenEvaluated(plugin) {
        fn(config)
    }
}

private enum class DependencyConfigurationName {
    API, IMPLEMENTATION, COMPILE_ONLY
}

fun Project.configureMultiplatformDependency(conf: SuspendTransformPluginExtension) {
    if (!conf.enabled.get()) {
        logger.info(
            "The `SuspendTransformGradleExtension.enable` in project {} for multiplatform is `false`, skip config.",
            this,
        )
        return
    }

    val sourceSetsByCompilation = sourceSetsByCompilation()
    project.logger.info("Suspend transform sourceSetsByCompilation: $sourceSetsByCompilation")

    // 时间久远，已经忘记为什么要做这个判断了，也忘记这段是在哪儿参考来的了💀
    if (rootProject.getBooleanProperty("kotlin.mpp.enableGranularSourceSetsMetadata")) {
        val multiplatformExtensions = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        val commonMainSourceSets =
            multiplatformExtensions.sourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
        val commonTestSourceSets =
            multiplatformExtensions.sourceSets.getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)

        if (conf.includeAnnotation.get()) {
            val notation = getDependencyNotation(
                SuspendTransPluginConstants.ANNOTATION_GROUP,
                SuspendTransPluginConstants.ANNOTATION_NAME,
                Platform.MULTIPLATFORM,
                conf.annotationDependency.flatMap { it.version }
            )
            dependencies.add(commonMainSourceSets.compileOnlyConfigurationName, notation)
            dependencies.add(commonTestSourceSets.implementationConfigurationName, notation)
        }

        if (conf.includeRuntime.get()) {
            val notation = getDependencyNotation(
                SuspendTransPluginConstants.RUNTIME_GROUP,
                SuspendTransPluginConstants.RUNTIME_NAME,
                Platform.MULTIPLATFORM,
                conf.runtimeDependency.flatMap { it.version }
            )
            dependencies.add(commonMainSourceSets.implementationConfigurationName, notation)
            dependencies.add(commonTestSourceSets.implementationConfigurationName, notation)
        }

        // For each source set that is only used in Native compilations, add an implementation dependency so that it
        // gets published and is properly consumed as a transitive dependency:
        sourceSetsByCompilation.forEach { (sourceSet, compilations) ->
            val isSharedSourceSet = compilations.all {
                it.platformType == KotlinPlatformType.common || it.platformType == KotlinPlatformType.native
                        || it.platformType == KotlinPlatformType.js || it.platformType == KotlinPlatformType.wasm
            }

            if (isSharedSourceSet) {
                if (conf.includeAnnotation.get()) {
                    val notation = getDependencyNotation(
                        SuspendTransPluginConstants.ANNOTATION_GROUP,
                        SuspendTransPluginConstants.ANNOTATION_NAME,
                        Platform.MULTIPLATFORM,
                        conf.annotationDependency.flatMap { it.version }
                    )
                    val configuration = sourceSet.implementationConfigurationName
                    dependencies.add(configuration, notation)
                }

                if (conf.includeRuntime.get()) {
                    val notation = getDependencyNotation(
                        SuspendTransPluginConstants.RUNTIME_GROUP,
                        SuspendTransPluginConstants.RUNTIME_NAME,
                        Platform.MULTIPLATFORM,
                        conf.runtimeDependency.flatMap { it.version }
                    )
                    val configuration = sourceSet.implementationConfigurationName
                    dependencies.add(configuration, notation)
                }
            }
        }
    } else {
        sourceSetsByCompilation.forEach { (sourceSet, compilations) ->
            val platformTypes = compilations.map { it.platformType }.toSet()
            logger.info(
                "Configure sourceSet [{}]. compilations: {}, platformTypes: {}",
                sourceSet,
                compilations,
                platformTypes
            )

            // TODO 可能同一个 sourceSet 会出现重复，但是需要处理吗？
            for (compilation in compilations) {
                val platformType = compilation.platformType
                val compilationName = compilation.compilationName
                val compilationType = compilationName.compilationNameToType()

                logger.info(
                    "compilation platformType: {}, compilationName: {}, compilationType: {}",
                    platformType,
                    compilationName,
                    compilationType
                )

                val platform = if (platformTypes.size > 1) {
                    Platform.MULTIPLATFORM
                } else {
                    // mix of platform types -> "common"
                    when (platformType) {
                        KotlinPlatformType.common -> Platform.MULTIPLATFORM
                        KotlinPlatformType.jvm, KotlinPlatformType.androidJvm -> Platform.JVM
                        KotlinPlatformType.js -> Platform.JS
                        KotlinPlatformType.native, KotlinPlatformType.wasm -> Platform.NATIVE
                    }
                }

                if (conf.includeAnnotation.get()) {
                    val configurationName = when {
                        // impl dependency for native (there is no transformation)
                        platform == Platform.NATIVE -> IMPLEMENTATION // sourceSet.implementationConfigurationName
                        // compileOnly dependency for JVM main compilation (jvmMain, androidMain)
                        compilationType == CompilationType.MAIN &&
                                platform == Platform.JVM -> COMPILE_ONLY // sourceSet.compileOnlyConfigurationName
                        // impl dependency for tests, and others
                        else -> IMPLEMENTATION // sourceSet.implementationConfigurationName
                    }

                    val notation = getDependencyNotation(
                        SuspendTransPluginConstants.ANNOTATION_GROUP,
                        SuspendTransPluginConstants.ANNOTATION_NAME,
                        platform,
                        conf.annotationDependency.flatMap { it.version }
                    )

                    sourceSet.dependencies {
                        when (configurationName) {
                            API -> {
                                api(notation)
                            }

                            IMPLEMENTATION -> {
                                implementation(notation)
                            }

                            COMPILE_ONLY -> {
                                compileOnly(notation)
                            }
                        }
                    }

                    // dependencies.add(configurationName, notation)
                    logger.debug(
                        "Add annotation dependency: {} {} for sourceSet {}",
                        configurationName,
                        notation,
                        sourceSet
                    )
                }

                if (conf.includeRuntime.get()) {
                    // val configurationName = sourceSet.implementationConfigurationName

                    val notation = getDependencyNotation(
                        SuspendTransPluginConstants.RUNTIME_GROUP,
                        SuspendTransPluginConstants.RUNTIME_NAME,
                        platform,
                        conf.runtimeDependency.flatMap { it.version }
                    )
                    sourceSet.dependencies {
                        implementation(notation)
                    }

                    logger.debug(
                        "Add runtime dependency: {} {} for sourceSet {}",
                        IMPLEMENTATION,
                        notation,
                        sourceSet
                    )
                }
            }
        }
    }
}

fun Project.withKotlinTargets(fn: (KotlinTarget) -> Unit) {
    extensions.findByType(KotlinTargetsContainer::class.java)?.let { kotlinExtension ->
        // find all compilations given sourceSet belongs to
        kotlinExtension.targets
            .all { target -> fn(target) }
    }
}

fun Project.sourceSetsByCompilation(): Map<KotlinSourceSet, List<KotlinCompilation<*>>> {
    val sourceSetsByCompilation = mutableMapOf<KotlinSourceSet, MutableList<KotlinCompilation<*>>>()
    withKotlinTargets { target ->
        target.compilations.forEach { compilation ->
            compilation.allKotlinSourceSets.forEach { sourceSet ->
                sourceSetsByCompilation.getOrPut(sourceSet) { mutableListOf() }.add(compilation)
            }
        }
    }
    return sourceSetsByCompilation
}

private enum class CompilationType { MAIN, TEST }

private fun String.compilationNameToType(): CompilationType? = when (this) {
    KotlinCompilation.MAIN_COMPILATION_NAME -> CompilationType.MAIN
    KotlinCompilation.TEST_COMPILATION_NAME -> CompilationType.TEST
    else -> null
}

private val Project.config: SuspendTransformPluginExtension
    get() = configOrNull ?: objects.newInstance(SuspendTransformPluginExtension::class.java)

private val Project.configOrNull: SuspendTransformPluginExtension?
    get() = extensions.findByType(SuspendTransformPluginExtension::class.java)

private enum class Platform(val suffix: String) {
    JVM("-jvm"), JS("-js"), NATIVE(""), MULTIPLATFORM("")
}

private fun getDependencyNotation(group: String, name: String, platform: Platform, version: String): String =
    "$group:$name${platform.suffix}:$version"

private fun getDependencyNotation(
    group: String,
    name: String,
    platform: Platform,
    version: Provider<String>
): Provider<String> =
    version.map { versionValue -> getDependencyNotation(group, name, platform, versionValue) }

private fun Project.getBooleanProperty(name: String) =
    rootProject.findProperty(name)?.toString()?.toBooleanStrict() ?: false
