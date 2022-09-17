package love.forte.plugin.suspendtrans.gradle

import BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("suspendTransform", SuspendTransformGradleExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
            artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
            version = BuildConfig.KOTLIN_PLUGIN_VERSION
        )
    }


    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(SuspendTransformGradleExtension::class.java)

        val dependencies = project.dependencies
        dependencies.add(
            "compileOnly",
            "${BuildConfig.ANNOTATION_GROUP}:${BuildConfig.ANNOTATION_NAME}:${BuildConfig.ANNOTATION_VERSION}"
        )
        if (extension.includeRuntime) {
            dependencies.add(
                extension.runtimeConfigurationName,
                "${BuildConfig.RUNTIME_GROUP}:${BuildConfig.RUNTIME_NAME}:${BuildConfig.RUNTIME_VERSION}"
            )
        }

        return project.provider {
            extension.toSubpluginOptions()
        }
    }

}


private fun SuspendTransformGradleExtension.toSubpluginOptions(): List<SubpluginOption> {
    return listOf()
//    val jvm = this.jvm
//    val js = this.js
//    return listOf(
//        SubpluginOption("enabled", enabled.toString()),
//        SubpluginOption("includeTransformRuntime", includeTransformRuntime.toString()),
//        SubpluginOption("runtimeConfigurationName", runtimeConfigurationName),
//        // jvm
//        SubpluginOption("jvm.jvmBlockingFunctionName", jvm.jvmBlockingFunctionName),
//        SubpluginOption("jvm.jvmBlockingMarkAnnotation.annotationName", jvm.jvmBlockingMarkAnnotation.annotationName),
//        SubpluginOption("jvm.jvmBlockingMarkAnnotation.baseNameProperty", jvm.jvmBlockingMarkAnnotation.baseNameProperty),
//        SubpluginOption("jvm.jvmBlockingMarkAnnotation.suffixProperty", jvm.jvmBlockingMarkAnnotation.suffixProperty),
//        SubpluginOption("jvm.jvmBlockingMarkAnnotation.asPropertyProperty", jvm.jvmBlockingMarkAnnotation.asPropertyProperty),
//
//        SubpluginOption("jvm.jvmAsyncFunctionName", jvm.jvmAsyncFunctionName),
//        SubpluginOption("jvm.jvmAsyncMarkAnnotation.annotationName", jvm.jvmAsyncMarkAnnotation.annotationName),
//        SubpluginOption("jvm.jvmAsyncMarkAnnotation.baseNameProperty", jvm.jvmAsyncMarkAnnotation.baseNameProperty),
//        SubpluginOption("jvm.jvmAsyncMarkAnnotation.suffixProperty", jvm.jvmAsyncMarkAnnotation.suffixProperty),
//        SubpluginOption("jvm.jvmAsyncMarkAnnotation.asPropertyProperty", jvm.jvmAsyncMarkAnnotation.asPropertyProperty),
//
////        SubpluginOption("jvm.originFunctionIncludeAnnotations", jvm.originFunctionIncludeAnnotations.toString()),
//
////        SubpluginOption("jvm.syntheticBlockingFunctionIncludeAnnotations", jvm.syntheticBlockingFunctionIncludeAnnotations.toString()),
//        SubpluginOption("jvm.copyAnnotationsToSyntheticBlockingFunction", jvm.copyAnnotationsToSyntheticBlockingFunction.toString()),
////        SubpluginOption("jvm.copyAnnotationsToSyntheticBlockingFunctionExcludes", jvm.copyAnnotationsToSyntheticBlockingFunctionExcludes.toString()),
////        SubpluginOption("jvm.syntheticAsyncFunctionIncludeAnnotations", jvm.syntheticAsyncFunctionIncludeAnnotations.toString()),
//        SubpluginOption("jvm.copyAnnotationsToSyntheticAsyncFunction", jvm.copyAnnotationsToSyntheticAsyncFunction.toString()),
////        SubpluginOption("jvm.copyAnnotationsToSyntheticAsyncFunctionExcludes", jvm.copyAnnotationsToSyntheticAsyncFunctionExcludes.toString()),
//
//        // js
//        SubpluginOption("js.jsPromiseFunctionName", js.jsPromiseFunctionName),
//        SubpluginOption("js.jsPromiseMarkAnnotation.annotationName", js.jsPromiseMarkAnnotation.annotationName),
//        SubpluginOption("js.jsPromiseMarkAnnotation.baseNameProperty", js.jsPromiseMarkAnnotation.baseNameProperty),
//        SubpluginOption("js.jsPromiseMarkAnnotation.suffixProperty", js.jsPromiseMarkAnnotation.suffixProperty),
//        SubpluginOption("js.jsPromiseMarkAnnotation.asPropertyProperty", js.jsPromiseMarkAnnotation.asPropertyProperty),
//
////        SubpluginOption("js.originFunctionIncludeAnnotations", enabled.toString()),
////        SubpluginOption("js.syntheticAsyncFunctionIncludeAnnotations", enabled.toString()),
//        SubpluginOption("js.copyAnnotationsToSyntheticAsyncFunction", js.copyAnnotationsToSyntheticAsyncFunction.toString()),
////        SubpluginOption("js.copyAnnotationsToSyntheticAsyncFunctionExcludes", enabled.toString()),
//    )

}