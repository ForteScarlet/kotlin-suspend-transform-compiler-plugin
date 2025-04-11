import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

    // testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit5"))

    testImplementation(kotlin("compiler"))
    testImplementation(kotlin("reflect"))

//    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
//    testImplementation("org.bitbucket.mstrobel:procyon-compilertools:0.6.0")

    testImplementation(libs.kotlinx.coroutines.core)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        // optIn.addAll(
        //     "kotlin.RequiresOptIn",
        //     "org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI"
        // )
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
//            "-opt-in=kotlin.RequiresOptIn",
//            "-opt-in=org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI",
        )
    }
}

tasks.withType(KotlinCompile::class.java).configureEach {
    // see https://youtrack.jetbrains.com/issue/KTIJ-21563
    // see https://youtrack.jetbrains.com/issue/KT-57297
//    kotlinOptions {
//        languageVersion = "1.9"
//        apiVersion = "1.9"
//    }
}

repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}

// buildConfig {
//     useKotlinOutput {
//         internalVisibility = true
//     }
//     withoutPackage()
//     buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.extra["kotlin_plugin_id"]}\"")
// }
