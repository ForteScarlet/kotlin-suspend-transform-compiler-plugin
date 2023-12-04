import love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension

plugins {
    kotlin("js")
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
         classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.5.1")
     }
 }


kotlin {
    js(IR) {
        nodejs()
        binaries.executable()
    }
}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

dependencies {
    implementation(kotlin("stdlib"))
//    val pluginVersion = "0.4.0"
//    api("love.forte.plugin.suspend-transform:suspend-transform-runtime:$pluginVersion")
//    api("love.forte.plugin.suspend-transform:suspend-transform-annotation:$pluginVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

extensions.getByType<SuspendTransformGradleExtension>().apply {
    println(this)
    useJsDefault()
}
