package love.forte.plugin.suspendtrans.gradle

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
        val extension = project.extensions.getByType(SuspendTransformGradleExtension::class.java)


//        val dependencies = project.dependencies
//        dependencies.add(
//            "compileOnly",
//            "${SuspendTransPluginConstants.ANNOTATION_GROUP}:${SuspendTransPluginConstants.ANNOTATION_NAME}:${SuspendTransPluginConstants.ANNOTATION_VERSION}"
//        )
//        if (extension.includeRuntime) {
//            dependencies.add(
//                extension.runtimeConfigurationName,
//                "${SuspendTransPluginConstants.RUNTIME_GROUP}:${SuspendTransPluginConstants.RUNTIME_NAME}:${SuspendTransPluginConstants.RUNTIME_VERSION}"
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
                SuspendTransPluginConstants.ANNOTATION_GROUP,
                SuspendTransPluginConstants.ANNOTATION_NAME,
                platform,
                conf.annotationDependencyVersion
            )
            if (platform == Platform.JVM) {
                dependencies.add(conf.annotationConfigurationName, notation)
            } else {
                // JS, native 似乎不支持其他的 name，例如 compileOnly
                dependencies.add("implementation", notation)
            }
            dependencies.add("testImplementation", notation)
        }
        if (conf.includeRuntime) {
            val notation = getDependencyNotation(
                SuspendTransPluginConstants.RUNTIME_GROUP,
                SuspendTransPluginConstants.RUNTIME_NAME,
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
        val multiplatformExtensions = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        val commonMainSourceSets =
            multiplatformExtensions.sourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
        val commonTestSourceSets =
            multiplatformExtensions.sourceSets.getByName(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)

        if (conf.includeAnnotation) {
            val notation = getDependencyNotation(
                SuspendTransPluginConstants.ANNOTATION_GROUP,
                SuspendTransPluginConstants.ANNOTATION_NAME,
                Platform.MULTIPLATFORM,
                conf.annotationDependencyVersion
            )
            dependencies.add(commonMainSourceSets.compileOnlyConfigurationName, notation)
            dependencies.add(commonTestSourceSets.implementationConfigurationName, notation)
        }

        if (conf.includeRuntime) {
            val notation = getDependencyNotation(
                SuspendTransPluginConstants.RUNTIME_GROUP,
                SuspendTransPluginConstants.RUNTIME_NAME,
                Platform.MULTIPLATFORM,
                conf.annotationDependencyVersion
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
                if (conf.includeAnnotation) {
                    val notation = getDependencyNotation(
                        SuspendTransPluginConstants.ANNOTATION_GROUP,
                        SuspendTransPluginConstants.ANNOTATION_NAME,
                        Platform.MULTIPLATFORM,
                        conf.annotationDependencyVersion
                    )
                    val configuration = sourceSet.implementationConfigurationName
                    dependencies.add(configuration, notation)
                }

                if (conf.includeRuntime) {
                    val notation = getDependencyNotation(
                        SuspendTransPluginConstants.RUNTIME_GROUP,
                        SuspendTransPluginConstants.RUNTIME_NAME,
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
                    SuspendTransPluginConstants.ANNOTATION_GROUP,
                    SuspendTransPluginConstants.ANNOTATION_NAME,
                    platform,
                    conf.annotationDependencyVersion
                )
                dependencies.add(configurationName, notation)
            }

            if (conf.includeRuntime) {
                val configurationName = sourceSet.implementationConfigurationName

                val notation = getDependencyNotation(
                    SuspendTransPluginConstants.RUNTIME_GROUP,
                    SuspendTransPluginConstants.RUNTIME_NAME,
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
