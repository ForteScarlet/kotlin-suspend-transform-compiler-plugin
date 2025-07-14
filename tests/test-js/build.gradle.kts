@file:OptIn(ExperimentalReturnTypeOverrideGenericApi::class)

import love.forte.plugin.suspendtrans.annotation.ExperimentalReturnTypeOverrideGenericApi
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.kotlinJsExportIgnoreClassInfo
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("love.forte.plugin.suspend-transform")
}

repositories {
    mavenLocal()
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes"
        )
    }

    js(IR) {
        nodejs()

        useEsModules()
        generateTypeScriptDefinitions()
        binaries.executable()

        compilerOptions {
            target = "es2015"
            useEsClasses = true
            freeCompilerArgs.addAll(
                // https://kotlinlang.org/docs/whatsnew20.html#per-file-compilation-for-kotlin-js-projects
                "-Xir-per-file",
            )
        }
    }

    sourceSets {
        jsMain.dependencies {
            implementation(kotlin("stdlib"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":runtime:suspend-transform-annotation"))
            implementation(project(":runtime:suspend-transform-runtime"))
            implementation(project(":tests:test-runner"))
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

suspendTransformPlugin {
    includeRuntime = false
    includeAnnotation = false
    transformers {
        addJsPromise {
            addOriginFunctionIncludeAnnotation {
                classInfo {
                    from(kotlinJsExportIgnoreClassInfo)
                }
            }

            addCopyAnnotationExclude {
                from(kotlinJsExportIgnoreClassInfo)
            }
        }
        addJsPromise {
            addOriginFunctionIncludeAnnotation {
                classInfo {
                    from(kotlinJsExportIgnoreClassInfo)
                }
            }
            
            markAnnotation {
                classInfo {
                    packageName = "love.forte.suspendtrans.test.runner"
                    className = "JsResultPromise"
                }
                hasReturnTypeOverrideGeneric = true
            }

            transformFunctionInfo {
                packageName = "love.forte.suspendtrans.test.runner"
                functionName = "jsResultToAsync"
            }

            addCopyAnnotationExclude {
                from(kotlinJsExportIgnoreClassInfo)
            }
        }
        // love.forte.suspendtrans.test.runner
    }
}
