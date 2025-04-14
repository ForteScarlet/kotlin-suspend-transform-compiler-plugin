rootProject.name = "kotlin-suspend-transform-compiler-plugin"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        // mavenLocal()
    }
}

include(":compiler:suspend-transform-plugin")
include(":compiler:suspend-transform-plugin-cli")
include(":compiler:suspend-transform-plugin-deprecated-configuration")
include(":compiler:suspend-transform-plugin-configuration")
include(":compiler:suspend-transform-plugin-embeddable")

include(":runtime:suspend-transform-annotation")
include(":runtime:suspend-transform-runtime")

include(":plugins:suspend-transform-plugin-gradle")

// include(":local-helper")

//Samples
include(":tests:test-jvm")
// include(":tests:test-js")
// include(":tests:test-kmp")
// include(":tests:test-android")
