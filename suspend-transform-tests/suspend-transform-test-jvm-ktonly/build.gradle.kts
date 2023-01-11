 buildscript {
     this@buildscript.repositories {
         mavenLocal()
         mavenCentral()
     }
 }
 
plugins {
    kotlin("jvm")
    id("love.forte.plugin.suspend-transform") version "0.2.0"
    // id(project(":suspend-transform-plugin-gradle"))
}


//withType<JavaCompile> {
//    sourceCompatibility = "11"
//    targetCompatibility = "11"
//}
//withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions.jvmTarget = "11"
//}




kotlin {
    dependencies {
        implementation(kotlin("stdlib"))
        implementation("love.forte.plugin.suspend-transform:suspend-transform-runtime:0.2.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    }
}
