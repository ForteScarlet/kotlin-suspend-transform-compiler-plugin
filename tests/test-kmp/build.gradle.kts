import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.kotlinJsExportIgnoreClassInfo
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.kotlinOptInClassInfo

plugins {
    kotlin("multiplatform")
    id("love.forte.plugin.suspend-transform")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

kotlin {
    jvm()
    js {
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
        commonMain.dependencies {
            implementation(kotlin("reflect"))
            implementation(project(":runtime:suspend-transform-annotation"))
            implementation(project(":runtime:suspend-transform-runtime"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

suspendTransformPlugin {
    includeRuntime = false
    includeAnnotation = false
    transformers {
        addJvmBlocking {
            addCopyAnnotationExclude {
                from(kotlinOptInClassInfo)
            }
        }
        addJvmAsync {
            addCopyAnnotationExclude {
                from(kotlinOptInClassInfo)
            }
        }

        addJsPromise {
            addCopyAnnotationExclude {
                from(kotlinOptInClassInfo)
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
// //     useJvmDefault()
//     transformers[TargetPlatform.JVM] = mutableListOf(
//         // Add `kotlin.OptIn` to copyAnnotationExcludes
//         jvmBlockingTransformer.copy(
//             copyAnnotationExcludes = buildList {
//                 addAll(jvmBlockingTransformer.copyAnnotationExcludes)
//                 add(ClassInfo("kotlin", "OptIn"))
//             }
//         ),
//
//         // Add `kotlin.OptIn` to copyAnnotationExcludes
//         jvmAsyncTransformer.copy(
//             copyAnnotationExcludes = buildList {
//                 addAll(jvmAsyncTransformer.copyAnnotationExcludes)
//                 add(ClassInfo("kotlin", "OptIn"))
//             }
//         )
//     )
// }

tasks.withType<Test> {
    useJUnitPlatform()
}
