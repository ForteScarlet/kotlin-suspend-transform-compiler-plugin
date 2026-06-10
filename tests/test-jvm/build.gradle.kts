import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations
import love.forte.plugin.suspendtrans.configuration.TransformReturnTypeGenericMode
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
    api(libs.kotlinx.coroutines.reactive)
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
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "love.forte.plugin.suspendtrans.sample"
                    className = "JvmNullableAsync"
                }
                defaultSuffix = "NullableAsync"
            }

            transformFunctionInfo {
                from(SuspendTransformConfigurations.jvmAsyncTransformFunction)
            }

            transformReturnType {
                packageName = "java.util.concurrent"
                className = "CompletableFuture"
            }
            transformReturnTypeGeneric = true
            transformReturnTypeGenericMode = TransformReturnTypeGenericMode.NULLABLE

            addOriginFunctionIncludeAnnotation {
                classInfo {
                    from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
                }
            }
            addSyntheticFunctionIncludeAnnotation {
                classInfo {
                    from(SuspendTransformConfigurations.jvmApi4JAnnotationClassInfo)
                }
                includeProperty = true
            }
        }
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "love.forte.plugin.suspendtrans.sample"
                    className = "JvmNonNullAsync"
                }
                defaultSuffix = "NonNullAsync"
            }

            transformFunctionInfo {
                from(SuspendTransformConfigurations.jvmAsyncTransformFunction)
            }

            transformReturnType {
                packageName = "java.util.concurrent"
                className = "CompletableFuture"
            }
            transformReturnTypeGeneric = true
            transformReturnTypeGenericMode = TransformReturnTypeGenericMode.NON_NULL

            addOriginFunctionIncludeAnnotation {
                classInfo {
                    from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
                }
            }
            addSyntheticFunctionIncludeAnnotation {
                classInfo {
                    from(SuspendTransformConfigurations.jvmApi4JAnnotationClassInfo)
                }
                includeProperty = true
            }
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
