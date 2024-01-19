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
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.7.0-dev1")
    }
}


//withType<JavaCompile> {
//    sourceCompatibility = "11"
//    targetCompatibility = "11"
//}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

dependencies {
    api(kotlin("stdlib"))
//    val pluginVersion = "0.4.0"
//    api("love.forte.plugin.suspend-transform:suspend-transform-runtime:$pluginVersion")
//    api("love.forte.plugin.suspend-transform:suspend-transform-annotation:$pluginVersion")
//    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.")
    api(libs.kotlinx.coroutines.core)
}

extensions.getByType<SuspendTransformGradleExtension>().apply {
    println(this)
    this.useJvmDefault()
}
