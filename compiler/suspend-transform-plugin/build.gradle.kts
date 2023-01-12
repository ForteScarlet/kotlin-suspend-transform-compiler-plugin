import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.gmazzo.buildconfig")
    //id("com.bnorm.power.kotlin-power-assert")
    id("suspend-transform.jvm-maven-publish")
    id("com.bennyhuo.kotlin.plugin.embeddable.test") version "1.7.10.0"
}

//testWithEmbedded0()

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

//    compileOnly(kotlin("compiler-embeddable"))

    kapt("com.google.auto.service:auto-service:1.0.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")

    testImplementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(kotlin("reflect"))
//    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")

    testImplementation(project(":runtime:suspend-transform-annotation"))
    testImplementation(project(":runtime:suspend-transform-runtime"))

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
//    testImplementation("org.bitbucket.mstrobel:procyon-compilertools:0.6.0")
//    testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:1.7.10.2-SNAPSHOT")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=enable", "-opt-in=kotlin.RequiresOptIn")

repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }
    withoutPackage()
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.extra["kotlin_plugin_id"]}\"")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
