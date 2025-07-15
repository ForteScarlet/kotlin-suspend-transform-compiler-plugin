rootProject.name = "kotlin-compiler-plugin-issue-reproduction"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":compiler-plugin")
include(":compiler-plugin-embeddable")
include(":annotation")
include(":gradle-plugin")
include(":test-app")
