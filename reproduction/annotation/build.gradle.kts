plugins {
    kotlin("multiplatform") version "2.0.0"
    id("maven-publish-multiplatform")
}

kotlin {
    explicitApi()

    jvm {
        jvmToolchain(8)
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
                implementation(kotlin("stdlib-common"))
            }
        }
    }
}
