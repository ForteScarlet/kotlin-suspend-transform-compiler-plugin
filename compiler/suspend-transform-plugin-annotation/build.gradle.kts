import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("suspend-transform.maven-publish")
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    configGradleBuildSrcFriendly()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
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

