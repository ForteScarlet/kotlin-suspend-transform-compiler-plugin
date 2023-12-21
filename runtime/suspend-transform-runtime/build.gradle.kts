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
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs()

//    val mainPresets = mutableSetOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()
//    val testPresets = mutableSetOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()
//
//    // K/N target supports
//    // https://kotlinlang.org/docs/native-target-support.html
//    val supportTargets = setOf(
//        // Tier 1
//        "linuxX64",
//        "macosX64",
//        "macosArm64",
//        "iosSimulatorArm64",
//        "iosX64",
//
//        // Tier 2
//        "linuxArm64",
//        "watchosSimulatorArm64",
//        "watchosX64",
//        "watchosArm32",
//        "watchosArm64",
//        "tvosSimulatorArm64",
//        "tvosX64",
//        "tvosArm64",
//        "iosArm64",
//
//        // Tier 3
//        "androidNativeArm32",
//        "androidNativeArm64",
//        "androidNativeX86",
//        "androidNativeX64",
//        "mingwX64",
//        "watchosDeviceArm64",
//    )
//
//    targets {
//        presets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset<*>>()
//            .filter { it.name in supportTargets }
//            .forEach { presets ->
//                val target = fromPreset(presets, presets.name)
//                val mainSourceSet = target.compilations["main"].kotlinSourceSets.first()
//                val testSourceSet = target.compilations["test"].kotlinSourceSets.first()
//                mainPresets.add(mainSourceSet)
//                testPresets.add(testSourceSet)
//            }
//    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":runtime:suspend-transform-annotation"))
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.jdk8)
        }
    }

}
