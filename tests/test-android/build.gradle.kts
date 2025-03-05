import jdk.tools.jlink.resources.plugins

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library") version "8.8.0"
    id("maven-publish")
    // id("com.github.ben-manes.versions")
    // id("love.forte.plugin.suspend-transform")
}

buildscript {
    this@buildscript.repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:2.1.0-0.11.0")
    }
}

//apply(plugin = "love.forte.plugin.suspend-transform")

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    sourceSets {
        val androidMain by getting {
            dependencies {
                api(project(":waltid-libraries:crypto:waltid-crypto"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
                implementation("io.github.oshai:kotlin-logging:7.0.4")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
                implementation("androidx.test.ext:junit:1.2.1")
                implementation("androidx.test:runner:1.6.1")
                implementation("androidx.test:rules:1.6.1")
            }
        }
    }
}

//extensions.getByType<SuspendTransformGradleExtension>().apply {
//    includeRuntime = false
//    includeAnnotation = false
////     useDefault()
//}
