pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
//     plugins {
//         id("love.forte.plugin.suspend-transform") version "0.2.0"
//     }
//    dependencies {
//        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.2.0")
//    }
}

rootProject.name = "suspend-transform-kotlin-compiler-plugin-tests"

//include(":suspend-transform-runtime")
//include(":suspend-transform-plugin")
//include(":suspend-transform-plugin-gradle")
 include(":suspend-transform-test-jvm")
 include(":suspend-transform-test-jvm-ktonly")
 include(":suspend-transform-test-js")

//include(":suspend-transform-plugin-ide:suspend-transform-plugin-idea")
