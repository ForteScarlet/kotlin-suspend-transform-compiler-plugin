import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    // id("com.github.gmazzo.buildconfig")
    id("suspend-transform.jvm-maven-publish")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))
    api(project(":compiler:suspend-transform-plugin-configuration"))
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.protobuf)

    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("compiler"))
    // testImplementation(libs.kotlinx.coroutines.core)
}

kotlin {
    configGradleBuildSrcFriendly()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.addAll("-Xjvm-default=all")
    }
}

tasks.test {
    useJUnitPlatform()
}
