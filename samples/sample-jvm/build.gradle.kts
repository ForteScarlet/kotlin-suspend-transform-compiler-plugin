import love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension

plugins {
    `java-library`
    kotlin("jvm")
//    id("love.forte.plugin.suspend-transform")
    // id("suspend-transform.jvm-maven-publish")
    // id(project(":suspend-transform-plugin-gradle"))
}


buildscript {
    this@buildscript.repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        //this.implementation()
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.8.0-beta1")
    }
}


//withType<JavaCompile> {
//    sourceCompatibility = "11"
//    targetCompatibility = "11"
//}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")

    }
}

//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions {
//        freeCompilerArgs += "-Xjvm-default=all"
        // useK2
//        languageVersion = "2.0"
//    }
//}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("test-junit5"))
    api(kotlin("reflect"))
//    val pluginVersion = "0.4.0"
//    api("love.forte.plugin.suspend-transform:suspend-transform-runtime:$pluginVersion")
//    api("love.forte.plugin.suspend-transform:suspend-transform-annotation:$pluginVersion")
//    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.")
    api(libs.kotlinx.coroutines.core)
}

extensions.getByType<SuspendTransformGradleExtension>().apply {
    useJvmDefault()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
