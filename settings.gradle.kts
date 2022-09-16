rootProject.name = "kotlin-suspend-transform-compiler-plugin"


pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
     plugins {
         id("love.forte.plugin.suspend-transform") version "0.0.1"
     }
}


include(":compiler:suspend-transform-plugin")
include(":compiler:suspend-transform-plugin-embeddable")

include(":runtime:suspend-transform-annotation")
include(":runtime:suspend-transform-runtime")

include(":plugins:suspend-transform-plugin-gradle")
include(":suspend-transform-plugin-sample")

// include(":plugins:ide:suspend-transform-plugin-idea")