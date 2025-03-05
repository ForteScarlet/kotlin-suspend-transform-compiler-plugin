import love.forte.plugin.suspendtrans.ClassInfo
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration.Companion.jvmAsyncTransformer
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration.Companion.jvmBlockingTransformer
import love.forte.plugin.suspendtrans.TargetPlatform
import love.forte.plugin.suspendtrans.gradle.SuspendTransformGradleExtension

plugins {
    kotlin("multiplatform")
}


buildscript {
    this@buildscript.repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:2.1.0-0.11.1")
    }
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
