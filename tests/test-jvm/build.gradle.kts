import love.forte.plugin.suspendtrans.ClassInfo
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration.Companion.jvmAsyncTransformer
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration.Companion.jvmBlockingTransformer
import love.forte.plugin.suspendtrans.TargetPlatform
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
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:2.1.0-Beta1-0.9.3")
        classpath("org.jetbrains.kotlin:kotlin-compiler:2.1.0-Beta1")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")

    }
}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("test-junit5"))
    api(kotlin("reflect"))
    api(project(":runtime:suspend-transform-annotation"))
    api(project(":runtime:suspend-transform-runtime"))
    api(libs.kotlinx.coroutines.core)
}

extensions.getByType<SuspendTransformGradleExtension>().apply {
    includeRuntime = false
    includeAnnotation = false
//     useJvmDefault()
    transformers[TargetPlatform.JVM] = mutableListOf(
        // Add `kotlin.OptIn` to copyAnnotationExcludes
        jvmBlockingTransformer.copy(
            copyAnnotationExcludes = buildList {
                addAll(jvmBlockingTransformer.copyAnnotationExcludes)
                add(ClassInfo("kotlin", "OptIn"))
            }
        ),

        // Add `kotlin.OptIn` to copyAnnotationExcludes
        jvmAsyncTransformer.copy(
            copyAnnotationExcludes = buildList {
                addAll(jvmAsyncTransformer.copyAnnotationExcludes)
                add(ClassInfo("kotlin", "OptIn"))
            }
        )
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}
