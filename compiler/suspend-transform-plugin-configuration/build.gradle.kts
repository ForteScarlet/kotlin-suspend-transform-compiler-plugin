import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    // id("com.github.gmazzo.buildconfig")
    id("suspend-transform.maven-publish")
}

dependencies {
    api(libs.kotlinx.serialization.core)
    api(project(":compiler:suspend-transform-plugin-annotation"))

    testImplementation(kotlin("test"))
}

kotlin {
    configGradleBuildSrcFriendly()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.addAll("-Xjvm-default=all",)
    }

}

tasks.test {
    useJUnitPlatform()
}

repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}

