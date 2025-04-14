// buildscript {
//     this@buildscript.repositories {
//         mavenLocal()
//         mavenCentral()
//     }
//     dependencies {
//         classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:2.1.20-0.12.0")
//     }
// }

plugins {
    `java-library`
    kotlin("jvm")
    id("love.forte.plugin.suspend-transform") version "2.1.20-0.11.1"
    // id("suspend-transform.jvm-maven-publish")
    // id(project(":suspend-transform-plugin-gradle"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")

    }
}

repositories {
    mavenLocal()
}

// apply(plugin = "love.forte.plugin.suspend-transform")

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("test-junit5"))
    api(kotlin("reflect"))
    api(project(":runtime:suspend-transform-annotation"))
    api(project(":runtime:suspend-transform-runtime"))
    api(libs.kotlinx.coroutines.core)
}

suspendTransform {

    // transformers {
    //     useJvmDefault()
    //
    // }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
