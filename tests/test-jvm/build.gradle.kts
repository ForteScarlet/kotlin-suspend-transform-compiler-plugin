@file:OptIn(ExperimentalReturnTypeOverrideGenericApi::class)

import love.forte.plugin.suspendtrans.annotation.ExperimentalReturnTypeOverrideGenericApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11

plugins {
    `java-library`
    kotlin("jvm")
    id("love.forte.plugin.suspend-transform")
    // id("suspend-transform.jvm-maven-publish")
    // id(project(":suspend-transform-plugin-gradle"))
}

kotlin {
    jvmToolchain(11)
    compilerOptions {
        jvmTarget = JVM_11
        javaParameters = true
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
    api(project(":tests:test-runner"))
}

// @Suppress("DEPRECATION")
// suspendTransform {
//     enabled = true
//     includeAnnotation = false
//     includeRuntime = false
//     useDefault()
// }

suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        useDefault()

        addJvmBlocking {
            markAnnotation {
                classInfo {
                    packageName = "love.forte.suspendtrans.test.runner"
                    className = "JvmResultBlock"
                }
                hasReturnTypeOverrideGeneric = true
            }
            transformFunctionInfo {
                packageName = "love.forte.suspendtrans.test.runner"
                functionName = "jvmResultToBlock"
            }
            // T itself
            // transformReturnType {
            // }
            transformReturnTypeGeneric = false
        }
        //
        addJvmAsync {
            markAnnotation {
                classInfo {
                    packageName = "love.forte.suspendtrans.test.runner"
                    className = "JvmResultAsync"
                }
                hasReturnTypeOverrideGeneric = true
            }
            transformFunctionInfo {
                packageName = "love.forte.suspendtrans.test.runner"
                functionName = "jvmResultToAsync"
            }
            // CompletableFuture<T>
            // transformReturnType {
            // }
            transformReturnTypeGeneric = true
        }

    }
}

/*
>     val blockingBaseName: String = "",
>     val blockingSuffix: String = "Blocking",
>     val blockingAsProperty: Boolean = false,
>
>     val asyncBaseName: String = "",
>     val asyncSuffix: String = "Async",
>     val asyncAsProperty: Boolean = false
 */

tasks.withType<Test> {
    useJUnitPlatform()
}
