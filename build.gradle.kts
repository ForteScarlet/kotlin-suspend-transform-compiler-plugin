import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    extra["kotlin_plugin_id"] = "love.forte.plugin.suspend-transform"
}

val ktVersion = libs.versions.kotlin.get()

group = IProject.GROUP
description = IProject.DESCRIPTION
version = ktVersion + "-" + IProject.pluginVersion.toString()

allprojects {
    group = IProject.GROUP
    description = IProject.DESCRIPTION
    version = ktVersion + "-" + IProject.pluginVersion.toString()

    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
        //maven {
        //    url = URI("")
        //}
    }
    this.tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.encoding = "UTF-8"
    }
    this.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            javaParameters.set(true)
        }
    }
}

apply(plugin = "suspend-transform.nexus-publish")

tasks.create("createChangelog") {
    group = "documentation"
    doFirst {
        val tag = "v$ktVersion-${IProject.pluginVersion}"
        val changelogDir = rootProject.file(".changelog").apply { mkdirs() }
        with(File(changelogDir, "$tag.md")) {
            if (!exists()) {
                createNewFile()
            }
            writeText("Kotlin version: `v$ktVersion`")
        }
    }
}
