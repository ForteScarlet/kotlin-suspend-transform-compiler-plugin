plugins {
    kotlin("jvm") version "2.0.0"
    id("application")
}

application {
    mainClass.set("com.example.test.TestAppKt")
}

dependencies {
    implementation(project(":annotation"))
    
    // This is needed to apply the compiler plugin to this module
    compileOnly(project(":compiler-plugin"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        // Enable the compiler plugin
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xplugin=${project.rootDir}/compiler-plugin-embeddable/build/libs/compiler-plugin-embeddable.jar"
        )
    }
}

kotlin {
    jvmToolchain(8)
}
