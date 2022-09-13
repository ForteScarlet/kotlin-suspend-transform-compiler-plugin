plugins {
    kotlin("js")
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

kotlin {
    js(IR) {
        nodejs()
        binaries.executable()
    }
}


kotlin {
    dependencies {
        implementation(project(":suspend-transform-runtime"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    }
}