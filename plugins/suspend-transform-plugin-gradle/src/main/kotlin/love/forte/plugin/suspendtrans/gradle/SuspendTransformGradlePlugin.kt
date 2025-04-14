package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.CliOptions
import love.forte.plugin.suspendtrans.cli.SuspendTransformCliOptions
import love.forte.plugin.suspendtrans.cli.encodeToHex
import love.forte.plugin.suspendtrans.gradle.DependencyConfigurationName.*
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        // TODO 想办法兼容过渡一下
        // try {
        //     Class.forName("love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension")
        //     target.extensions.create("suspendTransform", SuspendTransformGradleExtension::class.java)
        // } catch (ignore: ClassNotFoundException) {
        // } catch (ignore: NoClassDefFoundError) {
        // }

        val createdExtensions = target.extensions.create(
            "suspendTransformPlugin",
            SuspendTransformPluginExtension::class.java,
            // AbstractSuspendTransformPluginExtension::class.java,
        )

        createdExtensions.defaults(target.objects, target.providers)

        target.configureDependencies()
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project

        val isApplicable = project.plugins.hasPlugin(SuspendTransformGradlePlugin::class.java)
                && project.configOrNull?.enabled?.get() != false

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

        @Suppress("DEPRECATION") val oldExtension =
            project.extensions.getByType(SuspendTransformGradleExtension::class.java)
        if (oldExtension.transformers.isNotEmpty()) {
            project.logger.warn(
                "The `love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension` " +
                        "(`suspendTransform { ... }`) is deprecated, " +
                        "please use `love.forte.plugin.suspendtrans.gradle.SuspendTransformPluginExtension` " +
                        "(`suspendTransformPlugin { ... }`) instead."
            )
        }

        val extension = project.extensions.getByType(SuspendTransformPluginExtension::class.java)

        return project.provider {
            extension.toSubpluginOptions()
        }
    }
}


@Deprecated(
    message = "Use `SuspendTransformPluginExtension`",
    replaceWith = ReplaceWith(
        "SuspendTransformPluginExtension",
        "love.forte.plugin.suspendtrans.gradle.SuspendTransformPluginExtension"
    )
)
private fun SuspendTransformGradleExtension.toSubpluginOptions(): List<SubpluginOption> {
    return CliOptions.allOptions.map {
        SubpluginOption(it.oName, it.resolveToValue(this))
    }

}

private fun SuspendTransformPluginExtension.toSubpluginOptions(): List<SubpluginOption> {
    val cliConfig = SuspendTransformCliOptions.CLI_CONFIGURATION
    val configuration = toConfiguration()
    return listOf(SubpluginOption(cliConfig.optionName, configuration.encodeToHex()))
}


private fun Project.configureDependencies() {
    fun Project.include(platform: Platform, conf: SuspendTransformPluginExtension) {
        if (conf.enabled.get()) {
            logger.info(
                "The `SuspendTransformGradleExtension.enable` in project {} for platform {} is `false`, skip config.",
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
        sourceSetsByCompilation().forEach { (sourceSet, compilations) ->
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
        sourceSetsByCompilation().forEach { (sourceSet, compilations) ->
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

private fun getDependencyNotation(group: String, name: String, platform: Platform, version: Provider<String>): Provider<String> =
    version.map { versionValue -> getDependencyNotation(group, name, platform, versionValue) }

private fun Project.getBooleanProperty(name: String) =
    rootProject.findProperty(name)?.toString()?.toBooleanStrict() ?: false
