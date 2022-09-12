plugins {
    kotlin("multiplatform")
    id("love.forte.plugin.suspend-transform")
    // id(project(":suspend-transform-plugin-gradle"))
}

// buildscript {
//     this@buildscript.repositories {
//         mavenLocal()
//         mavenCentral()
//     }
//     dependencies {
//         classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:0.0.1")
//     }
// }

suspendTransform {
}


kotlin {
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
        //binaries.executable()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":suspend-transform-runtime"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        
        getByName("jvmMain") {
            dependencies {
                // runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
            }
        }
    }
    
}