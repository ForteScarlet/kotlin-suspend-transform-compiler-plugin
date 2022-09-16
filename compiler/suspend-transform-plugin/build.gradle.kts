import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
    id("com.github.gmazzo.buildconfig")
    //id("com.bnorm.power.kotlin-power-assert")
    id("suspend-transform.jvm-maven-publish")
    id("com.bennyhuo.kotlin.plugin.embeddable.test") version "1.7.10.0"
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))

    kapt("com.google.auto.service:auto-service:1.0.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")

    testImplementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("compiler-embeddable"))

    testImplementation(project(":runtime:suspend-transform-annotation"))
    testImplementation(project(":runtime:suspend-transform-runtime"))

//    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation("org.bitbucket.mstrobel:procyon-compilertools:0.6.0")
    testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:1.7.10.1")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
}
val compileKotlin: KotlinCompile by tasks

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }
    withoutPackage()
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$group.$name\"")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    // explicitApi()
}

//tasks.test {
//    useJUnitPlatform()
//}

// publishing {
//   publications {
//     create<MavenPublication>("default") {
//       from(components["java"])
//       artifact(tasks.kotlinSourcesJar)
//     }
//   }
// }
