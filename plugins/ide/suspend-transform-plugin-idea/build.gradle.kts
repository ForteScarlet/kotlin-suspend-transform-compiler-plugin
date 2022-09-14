import utils.by

plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.9.0"
    kotlin("kapt")
}

dependencies {
    implementation(project(":compiler:suspend-transform-plugin"))
    
    kapt("com.google.auto.service:auto-service:1.0.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    
}

intellij {
    version by "2022.2.1"
    type by "IC" // Target IDE Platform
    pluginName by "Kotlin Suspend Transform"
    
    plugins.set(listOf("java", "org.jetbrains.kotlin"))

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    
    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }
    //
    // signPlugin {
    //     certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    //     privateKey.set(System.getenv("PRIVATE_KEY"))
    //     password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    // }
    //
    // publishPlugin {
    //     token.set(System.getenv("PUBLISH_TOKEN"))
    // }
}


//
// dependencies {
//     implementation(project(":suspend-transform-plugin"))
// }
//
// intellij {
//     version.set("2022.2.1")
//     plugins.set(listOf("Kotlin", "com.intellij.gradle"))
//     pluginName.set("DeepCopy")
//     updateSinceUntilBuild.set(false)
// }
//
// tasks {
//     withType<PublishPluginTask> {
//         // project.property("intellij.token")?.let {
//         //     token.set(it.toString())
//         // }
//     }
// }