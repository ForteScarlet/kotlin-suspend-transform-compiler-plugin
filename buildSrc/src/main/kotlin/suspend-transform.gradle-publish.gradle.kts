import utils.systemProperty
import utils.by

plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") // version "1.0.0-rc-1"
}

pluginBundle {
    this.website = "https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin"
    this.vcsUrl = "https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin.git"
    this.tags = listOf("Kotlin", "Kotlinx Coroutines", "Kotlin Compiler Plugin")
    this.description = project.description
}

