import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.kotlinJsExportIgnoreClassInfo
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("love.forte.plugin.suspend-transform")
}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

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
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// suspendTransformPlugin {
//     includeRuntime = false
//     includeAnnotation = false
//     transformers {
//         addJsPromise {
//             addCopyAnnotationExclude {
//                 from(kotlinJsExportIgnoreClassInfo)
//             }
//         }
//     }
// }

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
    }
}

// extensions.getByType<SuspendTransformGradleExtension>().apply {
//     includeRuntime = false
//     includeAnnotation = false
//
//     transformers[TargetPlatform.JS] = mutableListOf(
//         SuspendTransformConfiguration.jsPromiseTransformer.copy(
//             copyAnnotationExcludes = listOf(
//                 ClassInfo("kotlin.js", "JsExport.Ignore")
//             )
//         )
//     )
//
// }
