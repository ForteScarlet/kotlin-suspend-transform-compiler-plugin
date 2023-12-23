import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("suspend-transform.multiplatform-maven-publish")
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    jvm {
        compilations.all {
            kotlinOptions {
                kotlinOptions {
                    jvmTarget = "1.8"
                    javaParameters = true
                }
            }
        }
    }

    js(IR) {
        nodejs()
        binaries.library()
    }

    // K/N target supports
    // https://kotlinlang.org/docs/native-target-support.html

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
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":runtime:suspend-transform-annotation"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        jsTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.jdk8)
        }

        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }

        nativeMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        nativeTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }

//        named("wasmJsMain").dependencies {
//            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
//        }
    }

}
