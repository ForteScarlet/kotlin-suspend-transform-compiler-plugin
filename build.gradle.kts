buildscript {
    extra["kotlin_plugin_id"] = "love.forte.plugin.suspend-transform"
}

allprojects {
    group = "love.forte.plugin.suspend-transform"
    version = "0.0.1"
    
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

// plugins {
//     kotlin("jvm") version "1.7.10" apply false
//     id("org.jetbrains.dokka") version "1.7.10" apply false
//     id("com.gradle.plugin-publish") version "1.0.0" apply false
//     id("com.github.gmazzo.buildconfig") version "3.1.0" apply false
// }

// dependencies {
//     testImplementation(kotlin("test"))
// }
//
// tasks.test {
//     useJUnitPlatform()
// }
//
// tasks.withType<KotlinCompile> {
//     kotlinOptions.jvmTarget = "1.8"
// }