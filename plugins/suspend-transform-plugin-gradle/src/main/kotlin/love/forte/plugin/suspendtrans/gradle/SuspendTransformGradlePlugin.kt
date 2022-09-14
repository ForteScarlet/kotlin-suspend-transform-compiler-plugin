package love.forte.plugin.suspendtrans.gradle

import BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("suspendTransform", SuspendTransformGradleExtension::class.java)
    }
    
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
    // {
    //     val platformType = kotlinCompilation.platformType
    //     if (platformType !in supportPlatforms) {
    //         return false
    //     }
    //
    //     return true
    // }
    
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
        return project.provider {
            // SubpluginOption()
            listOf(
            )
        }
    }
    
    companion object {
        private val supportPlatforms = setOf(
            KotlinPlatformType.androidJvm,
            KotlinPlatformType.jvm,
            KotlinPlatformType.js,
        )
    }
}