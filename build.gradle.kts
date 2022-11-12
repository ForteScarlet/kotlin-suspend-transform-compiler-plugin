buildscript {
    extra["kotlin_plugin_id"] = "love.forte.plugin.suspend-transform"
}

group = IProject.GROUP
version = IProject.VERSION
description = IProject.DESCRIPTION

allprojects {
    group = IProject.GROUP
    version = IProject.VERSION
    description = IProject.DESCRIPTION

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        //maven {
        //    url = URI("")
        //}
    }
    this.tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    this.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.javaParameters = true
    }
}

apply(plugin = "suspend-transform.nexus-publish")

