import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.github.gmazzo.buildconfig")
    id("com.bnorm.power.kotlin-power-assert")
    // id("kotlin-publish")
}

dependencies {
    // compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    compileOnly(kotlin("compiler-embeddable"))
    
    kapt("com.google.auto.service:auto-service:1.0.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    
    testImplementation(kotlin("test-junit5"))
    testImplementation(kotlin("compiler-embeddable"))
    
    testImplementation(project(":suspend-transform-runtime"))
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation("org.bitbucket.mstrobel:procyon-compilertools:0.6.0")
    
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
}

buildConfig {
    packageName(group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$group.$name\"")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileTestKotlin {
    kotlinOptions {
    
    }
}

// publishing {
//   publications {
//     create<MavenPublication>("default") {
//       from(components["java"])
//       artifact(tasks.kotlinSourcesJar)
//     }
//   }
// }
