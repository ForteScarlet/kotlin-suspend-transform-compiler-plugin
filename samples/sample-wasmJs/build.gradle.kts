import love.forte.plugin.suspendtrans.ClassInfo
import love.forte.plugin.suspendtrans.FunctionInfo
import love.forte.plugin.suspendtrans.MarkAnnotation
import love.forte.plugin.suspendtrans.TargetPlatform
import love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
//    id("org.jetbrains.compose") version "1.6.0-alpha01"
//    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.kotlinSerialization)

    alias(libs.plugins.jetbrainsCompose)
}

buildscript {
    this@buildscript.repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.7.0-dev1")
    }
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenCentral()
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
//        nodejs()
        moduleName = "suspendTransSample"
        browser {
            useEsModules()
            commonWebpackConfig {
                outputFileName = "suspendTransSample.js"
            }
        }
//        useEsModules()
//        generateTypeScriptDefinitions()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
//            implementation("androidx.compose.ui:ui:$composev")
//            implementation("androidx.compose.ui:ui-tooling:$composev")
//            implementation("androidx.compose.ui:ui-tooling-preview:$composev")
//            implementation("androidx.compose.foundation:foundation:$composev")
//            implementation("androidx.compose.material:material:$composev")
//            @OptIn(ExperimentalComposeLibrary::class)
//            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
        }
        named("wasmJsMain") {
            dependencies {
//                implementation(kotlin("stdlib"))
            }
        }
    }
}

compose.experimental {
    web.application {
    }
}
extensions.configure<SuspendTransformGradleExtension> {
    includeRuntime = false
    includeAnnotation = false

    addTransformers(
        TargetPlatform.WASM,
        love.forte.plugin.suspendtrans.Transformer(
            markAnnotation = MarkAnnotation(
                ClassInfo("wasmtrans", "JsPromise"),
                baseNameProperty = "baseName",
                suffixProperty = "suffix",
                defaultSuffix = "Async"
            ),
            transformFunctionInfo = FunctionInfo(
                packageName = "wasmtrans",
                className = null,
                functionName = "runInAsync"
            ),
            transformReturnType = ClassInfo("wasmtrans", "AsyncResult"),
            transformReturnTypeGeneric = true,
            originFunctionIncludeAnnotations = listOf(),
            copyAnnotationsToSyntheticFunction = true,
            copyAnnotationExcludes = listOf(ClassInfo("wasmtrans", "JsPromise")),
            syntheticFunctionIncludeAnnotations = listOf()
        )
    )

}

