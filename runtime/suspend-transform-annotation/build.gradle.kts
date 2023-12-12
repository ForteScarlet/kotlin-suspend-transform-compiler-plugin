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

//    withKotlinTargets { target ->
//        targets.findByName(target.name)?.compilations?.all {
//            // 'expect'/'actual' classes (including interfaces, objects, annotations, enums, and 'actual' typealiases) are in Beta. You can use -Xexpect-actual-classes flag to suppress this warning. Also see: https://youtrack.jetbrains.com/issue/KT-61573
//            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
//        }
//    }
}


//fun Project.withKotlinTargets(fn: (KotlinTarget) -> Unit) {
//    extensions.findByType(KotlinTargetsContainer::class.java)?.let { kotlinExtension ->
//        // find all compilations given sourceSet belongs to
//        kotlinExtension.targets.all { target -> fn(target) }
//    }
//}
