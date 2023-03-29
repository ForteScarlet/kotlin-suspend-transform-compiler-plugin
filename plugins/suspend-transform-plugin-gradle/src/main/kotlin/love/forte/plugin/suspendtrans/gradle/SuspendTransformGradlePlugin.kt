package love.forte.plugin.suspendtrans.gradle

import BuildConfig
import love.forte.plugin.suspendtrans.CliOptions
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
        target.extensions.create("suspendTransform", SuspendTransformGradleExtension::class.java)
        target.configureDependencies()
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.target.project.plugins.hasPlugin(SuspendTransformGradlePlugin::class.java)
    }

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
            artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
            version = BuildConfig.KOTLIN_PLUGIN_VERSION
        )
    }


    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val target = kotlinCompilation.target
        val project = target.project
        val extension = project.extensions.getByType(SuspendTransformGradleExtension::class.java)


//        val dependencies = project.dependencies
//        dependencies.add(
//            "compileOnly",
//            "${BuildConfig.ANNOTATION_GROUP}:${BuildConfig.ANNOTATION_NAME}:${BuildConfig.ANNOTATION_VERSION}"
//        )
//        if (extension.includeRuntime) {
//            dependencies.add(
//                extension.runtimeConfigurationName,
//                "${BuildConfig.RUNTIME_GROUP}:${BuildConfig.RUNTIME_NAME}:${BuildConfig.RUNTIME_VERSION}"
//            )
//        }

        return project.provider {
            extension.toSubpluginOptions()
        }
    }

}


private fun SuspendTransformGradleExtension.toSubpluginOptions(): List<SubpluginOption> {
    return CliOptions.allOptions.map {
        SubpluginOption(it.oName, it.resolveToValue(this))
    }

}


private fun Project.configureDependencies() {
    fun Project.include(platform: Platform, conf: SuspendTransformGradleExtension) {
        if (conf.includeAnnotation) {
            val notation = getDependencyNotation(
                BuildConfig.ANNOTATION_GROUP,
                BuildConfig.ANNOTATION_NAME,
                platform,
                conf.annotationDependencyVersion
            )
            dependencies.add(conf.annotationConfigurationName, notation)
            dependencies.add("testImplementation", notation)
        }
        if (conf.includeRuntime) {
            val notation = getDependencyNotation(
                BuildConfig.RUNTIME_GROUP,
                BuildConfig.RUNTIME_NAME,
                platform,
                conf.runtimeDependencyVersion
            )
            dependencies.add(conf.runtimeConfigurationName, notation)
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
    fn: Project.(conf: SuspendTransformGradleExtension) -> Unit
) {
    withPluginWhenEvaluated(plugin) {
        fn(config)
    }
}

fun Project.configureMultiplatformDependency(conf: SuspendTransformGradleExtension) {
    if (rootProject.getBooleanProperty("kotlin.mpp.enableGranularSourceSetsMetadata")) {
        val mainSourceSets = project.extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
            .getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
        val testSourceSets = project.extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
            .getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)


        if (conf.includeAnnotation) {
            val notation = getDependencyNotation(
                BuildConfig.ANNOTATION_GROUP,
                BuildConfig.ANNOTATION_NAME,
                Platform.MULTIPLATFORM,
                conf.annotationDependencyVersion
            )
            dependencies.add(mainSourceSets.compileOnlyConfigurationName, notation)
            dependencies.add(testSourceSets.implementationConfigurationName, notation)
        }

        if (conf.includeRuntime) {
            val notation = getDependencyNotation(
                BuildConfig.RUNTIME_GROUP,
                BuildConfig.RUNTIME_NAME,
                Platform.MULTIPLATFORM,
                conf.annotationDependencyVersion
            )
            dependencies.add(mainSourceSets.implementationConfigurationName, notation)
            dependencies.add(testSourceSets.implementationConfigurationName, notation)
        }

        // For each source set that is only used in Native compilations, add an implementation dependency so that it
        // gets published and is properly consumed as a transitive dependency:
        sourceSetsByCompilation().forEach { (sourceSet, compilations) ->
            val isSharedNativeSourceSet = compilations.all {
                it.platformType == KotlinPlatformType.common || it.platformType == KotlinPlatformType.native
            }
            if (isSharedNativeSourceSet) {
                if (conf.includeAnnotation) {
                    val notation = getDependencyNotation(
                        BuildConfig.ANNOTATION_GROUP,
                        BuildConfig.ANNOTATION_NAME,
                        Platform.MULTIPLATFORM,
                        conf.annotationDependencyVersion
                    )
                    val configuration = sourceSet.implementationConfigurationName
                    dependencies.add(configuration, notation)
                }

                if (conf.includeRuntime) {
                    val notation = getDependencyNotation(
                        BuildConfig.RUNTIME_GROUP,
                        BuildConfig.RUNTIME_NAME,
                        Platform.MULTIPLATFORM,
                        conf.annotationDependencyVersion
                    )
                    val configuration = sourceSet.implementationConfigurationName
                    dependencies.add(configuration, notation)
                }
            }
        }
    } else {
        sourceSetsByCompilation().forEach { (sourceSet, compilations) ->
            val platformTypes = compilations.map { it.platformType }.toSet()
            val compilationNames = compilations.map { it.compilationName }.toSet()
            if (compilationNames.size != 1)
                error("Source set '${sourceSet.name}' of project '$name' is part of several compilations $compilationNames")
            val compilationType = compilationNames.single().compilationNameToType()
                ?: return@forEach // skip unknown compilations
            val platform =
                if (platformTypes.size > 1) Platform.MULTIPLATFORM else // mix of platform types -> "common"
                    when (platformTypes.single()) {
                        KotlinPlatformType.common -> Platform.MULTIPLATFORM
                        KotlinPlatformType.jvm, KotlinPlatformType.androidJvm -> Platform.JVM
                        KotlinPlatformType.js -> Platform.JS
                        KotlinPlatformType.native, KotlinPlatformType.wasm -> Platform.NATIVE
                    }

            if (conf.includeAnnotation) {
                val configurationName = when {
                    // impl dependency for native (there is no transformation)
                    platform == Platform.NATIVE -> sourceSet.implementationConfigurationName
                    // compileOnly dependency for main compilation (commonMain, jvmMain, jsMain)
                    compilationType == CompilationType.MAIN -> sourceSet.compileOnlyConfigurationName
                    // impl dependency for tests
                    else -> sourceSet.implementationConfigurationName
                }

                val notation = getDependencyNotation(
                    BuildConfig.ANNOTATION_GROUP,
                    BuildConfig.ANNOTATION_NAME,
                    platform,
                    conf.annotationDependencyVersion
                )
                dependencies.add(configurationName, notation)
            }

            if (conf.includeRuntime) {
                val configurationName = sourceSet.implementationConfigurationName

                val notation = getDependencyNotation(
                    BuildConfig.RUNTIME_GROUP,
                    BuildConfig.RUNTIME_NAME,
                    platform,
                    conf.runtimeDependencyVersion
                )
                dependencies.add(configurationName, notation)
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
    val sourceSetsByCompilation = hashMapOf<KotlinSourceSet, MutableList<KotlinCompilation<*>>>()
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

private val Project.config: SuspendTransformGradleExtension
    get() = extensions.findByType(SuspendTransformGradleExtension::class.java) ?: SuspendTransformGradleExtension()

private enum class Platform(val suffix: String) {
    JVM("-jvm"), JS("-js"), NATIVE(""), MULTIPLATFORM("")
}

private fun getDependencyNotation(group: String, name: String, platform: Platform, version: String): String =
    "$group:$name${platform.suffix}:$version"

private fun Project.getBooleanProperty(name: String) =
    rootProject.findProperty(name)?.toString()?.toBooleanStrict() ?: false
