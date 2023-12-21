import love.forte.plugin.suspendtrans.ClassInfo
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.TargetPlatform
import love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

buildscript {
    this@buildscript.repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.6.0-local-test-1")
    }
}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        generateTypeScriptDefinitions()
        binaries.executable()
    }

    sourceSets {
        named("wasmJsMain") {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
    }
}

extensions.configure<SuspendTransformGradleExtension> {
    includeRuntime = false
    includeAnnotation = false

    transformers[TargetPlatform.JS] = mutableListOf(
        SuspendTransformConfiguration.jsPromiseTransformer.copy(
            copyAnnotationExcludes = listOf(
                ClassInfo("kotlin.js", "JsExport.Ignore")
            )
        )
    )
}

