// buildscript {
//     this@buildscript.repositories {
//         mavenLocal()
//         mavenCentral()
//     }
//     dependencies {
//         classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:2.1.20-0.12.0")
//     }
// }

plugins {
    `java-library`
    kotlin("jvm")
    id("love.forte.plugin.suspend-transform") version "2.1.20-0.12.0"
    // id("suspend-transform.jvm-maven-publish")
    // id(project(":suspend-transform-plugin-gradle"))
}

kotlin {
    compilerOptions {
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
}

suspendTransformPlugin {
    transformers {
        addJvm {
            originFunctionIncludeAnnotations
            // originFunctionIncludeAnnotations {
                // create("demo1") {
                //     classInfo { from(SuspendTransformConfigurations.jvmSyntheticClassInfo) }
                // }
            // }
        }
    }
}

// suspendTransformPlugin {
//     includeRuntime = false
//     transformers {
//         // addJvm {
//         //     originFunctionIncludeAnnotations {
//         //         create("Hi~") {
//         //             classInfo {
//         //             }
//         //         }
//         //     }
//         // }
//     }
//     // transformers {
//     //     addJvm {
//     //         originFunctionIncludeAnnotations.create("any name") {
//     //
//     //         }
//     //     }
//     // }
//     // transformers.addJvm {
//     //     originFunctionIncludeAnnotations
//     // }
// }

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
