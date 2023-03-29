plugins {
    kotlin("multiplatform")
    id("suspend-transform.multiplatform-maven-publish")
}

kotlin {
    explicitApi()
    
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

    val mainPresets = mutableSetOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()
    val testPresets = mutableSetOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()

    // K/N target supports
    // https://kotlinlang.org/docs/native-target-support.html
    val supportTargets = setOf(
        // Tier 1
        "linuxX64",
        "macosX64",
        "macosArm64",
        "iosSimulatorArm64",
        "iosX64",
        // Tier 2
//        "linuxArm64", // 1.7.0+
        "watchosSimulatorArm64",
        "watchosX64",
        "watchosArm32",
        "watchosArm64",
        "tvosSimulatorArm64",
        "tvosX64",
        "tvosArm64",
        "iosArm64",
        // Tier 3
//        "androidNativeArm32", // 1.7.0+
//        "androidNativeArm64", // 1.7.0+
//        "androidNativeX86", // 1.7.0+
//        "androidNativeX64", // 1.7.0+
        "mingwX64",
//        "watchosDeviceArm64", // 1.7.0+
    )

    targets {
        presets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset<*>>()
            .filter { it.name in supportTargets }
            .forEach { presets ->
                val target = fromPreset(presets, presets.name)
                val mainSourceSet = target.compilations["main"].kotlinSourceSets.first()
                val testSourceSet = target.compilations["test"].kotlinSourceSets.first()
                mainPresets.add(mainSourceSet)
                testPresets.add(testSourceSet)
            }
    }

    val coroutinesVersion = "1.6.4"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":runtime:suspend-transform-annotation"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            }
        }

        getByName("jvmMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val nativeTest by creating {
            dependsOn(commonTest)
        }

        configure(mainPresets) { dependsOn(nativeMain) }
        configure(testPresets) { dependsOn(nativeTest) }

    }

}
