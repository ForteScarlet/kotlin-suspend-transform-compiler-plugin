import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
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
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    extensions.findByType(KotlinTargetsContainer::class.java)?.also { kotlinExtension ->
        // find all compilations given sourceSet belongs to
        kotlinExtension.targets
            .all {
                targets.findByName(name)?.compilations?.all {
                    kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
                }
            }
    }

}


//fun Project.withKotlinTargets(fn: (KotlinTarget) -> Unit) {
//    extensions.findByType(KotlinTargetsContainer::class.java)?.let { kotlinExtension ->
//        // find all compilations given sourceSet belongs to
//        kotlinExtension.targets.all { target -> fn(target) }
//    }
//}
