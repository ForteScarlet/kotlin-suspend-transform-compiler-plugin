plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    id("suspend-transform.gradle-publish")
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }
    withoutPackage()
    val project = project(":suspend-transform-plugin-embeddable")
    
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.extra["kotlin_plugin_id"]}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
}

gradlePlugin {
    plugins {
        create("suspendTransform") {
            id = rootProject.extra["kotlin_plugin_id"] as String
            
            displayName = "Kotlin suspend function transformer"
            description = "Kotlin suspend function transformer"
            implementationClass = "love.forte.plugin.suspendtrans.gradle.SuspendTransformGradlePlugin"
        }
    }
}