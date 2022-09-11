allprojects {
    group = "love.forte.plugin"
    version = "1.0-SNAPSHOT"
    
    repositories {
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