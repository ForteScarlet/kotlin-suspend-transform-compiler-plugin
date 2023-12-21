import love.forte.gradle.common.core.project.setup
import utils.isCi

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    id("suspend-transform.jvm-maven-publish")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
}

setup(IProject)

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
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

//if (!isAutomatedGradlePluginPublishing()) {
if (isCi()) {
    @Suppress("UnstableApiUsage")
    gradlePlugin {
        website = "https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin"
        vcsUrl = "https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin.git"
        plugins {
            create("suspendTransform") {
                id = (rootProject.extra["kotlin_plugin_id"] as String)
                displayName = "Kotlin suspend function transformer"
                implementationClass = "love.forte.plugin.suspendtrans.gradle.SuspendTransformGradlePlugin"
                tags = listOf("Kotlin", "Kotlinx Coroutines", "Kotlin Compiler Plugin")
                description = IProject.DESCRIPTION
            }
        }
        //isAutomatedPublishing = true
    }
}


//publishing {
//    repositories {
//        mavenLocal()
//        gradlePluginPortal {
//            this.name = "GradleCentralPluginRepository"
//        }
//    }
//}
