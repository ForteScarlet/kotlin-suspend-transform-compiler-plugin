import love.forte.plugin.suspendtrans.ClassInfo
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
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
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.7.0-dev2")
    }
}

repositories {
    mavenLocal()
}

apply(plugin = "love.forte.plugin.suspend-transform")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        // useK2
        languageVersion = "2.0"
    }
}

kotlin {
    js(IR) {
        nodejs()
        useEsModules()
        generateTypeScriptDefinitions()
        binaries.executable()
        compilations.all {
            kotlinOptions {
                useEsClasses = true
            }
        }
    }

    sourceSets {
        named("jsMain") {
            dependencies {
                implementation(kotlin("stdlib"))
                //    val pluginVersion = "0.4.0"
                //    api("love.forte.plugin.suspend-transform:suspend-transform-runtime:$pluginVersion")
                //    api("love.forte.plugin.suspend-transform:suspend-transform-annotation:$pluginVersion")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}



extensions.getByType<SuspendTransformGradleExtension>().apply {
    transformers[TargetPlatform.JS] = mutableListOf(
        SuspendTransformConfiguration.jsPromiseTransformer.copy(
            copyAnnotationExcludes = listOf(
                ClassInfo("kotlin.js", "JsExport.Ignore")
            )
        )
    )

}
