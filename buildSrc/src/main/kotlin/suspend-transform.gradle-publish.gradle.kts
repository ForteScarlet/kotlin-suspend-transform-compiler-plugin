import gradle.kotlin.dsl.accessors._59a6e08710bd766406b34ec8c4dcd1fe.publishing
import gradle.kotlin.dsl.accessors._59a6e08710bd766406b34ec8c4dcd1fe.signing
import utils.systemProperty
import utils.by

plugins {
    id("java-gradle-plugin")
    `maven-publish`
    id("com.gradle.plugin-publish") // version "1.0.0-rc-1"
}

pluginBundle {
    website = "https://github.com/ForteScarlet/suspend-transform-kotlin-compiler-plugin"
    vcsUrl = "https://github.com/ForteScarlet/suspend-transform-kotlin-compiler-plugin.git"
    tags = listOf("Kotlin", "Kotlinx Coroutines", "Kotlin Compiler Plugin")
}
