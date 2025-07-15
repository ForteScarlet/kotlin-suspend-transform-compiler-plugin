plugins {
    kotlin("jvm") version "2.0.0"
    id("java-gradle-plugin")
    id("maven-publish-local")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
}

gradlePlugin {
    plugins {
        create("minimalCompilerPlugin") {
            id = "com.example.minimal-compiler-plugin"
            implementationClass = "com.example.plugin.gradle.MinimalCompilerGradlePlugin"
        }
    }
}

kotlin {
    jvmToolchain(8)
}
