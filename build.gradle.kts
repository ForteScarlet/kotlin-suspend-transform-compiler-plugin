import love.forte.gradle.common.core.project.setup

buildscript {
    extra["kotlin_plugin_id"] = "love.forte.plugin.suspend-transform"
}

setup(IProject)

allprojects {
    setup(IProject)

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
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.javaParameters = true
    }
}

apply(plugin = "suspend-transform.nexus-publish")

