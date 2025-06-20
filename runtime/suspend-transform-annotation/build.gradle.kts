import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("suspend-transform.maven-publish")
}


// https://kotlinlang.org/docs/gradle-compiler-options.html#how-to-define-options
//tasks.withType<KotlinCompile> {
//    compilerOptions {
//        jvmTarget.set(JvmTarget.JVM_1_8)
//    }
//}

//tasks.named<KotlinCompilationTask<*>>("compileKotlin").configure {
//    compilerOptions {
//        freeCompilerArgs.add("-Xexpect-actual-classes")
//    }
//}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            javaParameters.set(true)
        }
//        compilations.all {
//            this.kotlinOptions
//        }
//        compilations.all {
//            kotlinOptions {
//                jvmTarget = "1.8"
//                javaParameters = true
//            }
//        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    // tier1
    linuxX64()
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // tier2
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // tier3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()

    // wasm
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }


//    extensions.findByType(KotlinTargetsContainer::class.java)?.also { kotlinExtension ->
//        // find all compilations given sourceSet belongs to
//        kotlinExtension.targets
//            .all {
//                compilations.all {
//                    kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
//                }
//            }
//    }

}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}

//fun Project.withKotlinTargets(fn: (KotlinTarget) -> Unit) {
//    extensions.findByType(KotlinTargetsContainer::class.java)?.let { kotlinExtension ->
//        // find all compilations given sourceSet belongs to
//        kotlinExtension.targets.all { target -> fn(target) }
//    }
//}
