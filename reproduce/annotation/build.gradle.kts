
plugins {
    kotlin("multiplatform")
    id("suspend-transform.maven-publish")
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvmToolchain(11)
    jvm {
    }

    js(IR) {
        browser()
        nodejs()
    }

    // Add a few native targets for demonstration
    linuxX64()
    mingwX64()
    macosX64()

    sourceSets {
        commonMain {
            dependencies {
            }
        }
    }
}
