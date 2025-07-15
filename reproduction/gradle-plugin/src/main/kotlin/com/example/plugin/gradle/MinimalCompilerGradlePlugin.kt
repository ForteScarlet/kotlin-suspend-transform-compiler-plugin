package com.example.plugin.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.io.File

/**
 * Gradle plugin that applies the minimal compiler plugin to a project.
 */
class MinimalCompilerGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.configurations.create(PLUGIN_CLASSPATH_CONFIGURATION_NAME).apply {
            isVisible = false
            isTransitive = false
            description = "The classpath for the Minimal Kotlin compiler plugin"
        }

        target.dependencies.add(
            PLUGIN_CLASSPATH_CONFIGURATION_NAME,
            "${PLUGIN_GROUP_ID}:compiler-plugin-embeddable:${PLUGIN_VERSION}"
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = "com.example.minimal-compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = PLUGIN_GROUP_ID,
        artifactId = "compiler-plugin-embeddable",
        version = PLUGIN_VERSION
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        return project.provider { emptyList() }
    }

    override fun getPluginArtifactForNative(): SubpluginArtifact = SubpluginArtifact(
        groupId = PLUGIN_GROUP_ID,
        artifactId = "compiler-plugin-embeddable",
        version = PLUGIN_VERSION
    )

    companion object {
        private const val PLUGIN_GROUP_ID = "com.example"
        private const val PLUGIN_VERSION = "1.0.0"
        private const val PLUGIN_CLASSPATH_CONFIGURATION_NAME = "minimalCompilerPluginClasspath"
    }
}
