import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("suspend-transform.jvm-maven-publish")
}

dependencies {
    compileOnly(kotlin("compiler"))
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
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

