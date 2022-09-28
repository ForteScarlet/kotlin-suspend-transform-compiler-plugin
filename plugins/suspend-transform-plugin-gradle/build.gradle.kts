import utils.isCi

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    id("suspend-transform.jvm-maven-publish")
    id("suspend-transform.gradle-publish")
}

dependencies {
    compileOnly(kotlin("gradle-plugin-api"))
    api(project(":compiler:suspend-transform-plugin"))

}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }
    withoutPackage()

    val project = project(":compiler:suspend-transform-plugin-embeddable")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.extra["kotlin_plugin_id"]}\"")
    buildConfigField("String", "PLUGIN_VERSION", "\"${version}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")

    val annotationProject = project(":runtime:suspend-transform-annotation")
    buildConfigField("String", "ANNOTATION_GROUP", "\"${annotationProject.group}\"")
    buildConfigField("String", "ANNOTATION_NAME", "\"${annotationProject.name}\"")
    buildConfigField("String", "ANNOTATION_VERSION", "\"${annotationProject.version}\"")


    val runtimeProject = project(":runtime:suspend-transform-runtime")
    buildConfigField("String", "RUNTIME_GROUP", "\"${runtimeProject.group}\"")
    buildConfigField("String", "RUNTIME_NAME", "\"${runtimeProject.name}\"")
    buildConfigField("String", "RUNTIME_VERSION", "\"${runtimeProject.version}\"")


}

gradlePlugin {
    plugins {
        create("suspendTransform") {
            id = (rootProject.extra["kotlin_plugin_id"] as String)
            displayName = "Kotlin suspend function transformer"
            description = "Kotlin suspend function transformer"
            implementationClass = "love.forte.plugin.suspendtrans.gradle.SuspendTransformGradlePlugin"
        }
    }
    this.isAutomatedPublishing = isCi()
    // repo?
}

//publishing {
//    repositories {
//        mavenLocal()
//        gradlePluginPortal {
//            this.name = "Gradle Central Plugin Repository"
//        }
//    }
//}