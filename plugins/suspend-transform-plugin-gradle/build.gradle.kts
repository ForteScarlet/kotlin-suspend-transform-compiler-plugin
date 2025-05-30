import love.forte.gradle.common.core.Gpg
import love.forte.gradle.common.publication.configure.configPublishMaven
import love.forte.gradle.common.publication.configure.publishingExtension
import love.forte.gradle.common.publication.configure.setupPom
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import utils.isMainPublishable

plugins {
    `java-library`
    kotlin("jvm")
    id("suspend-transform.dokka-module")
    id("com.github.gmazzo.buildconfig")
//    `java-gradle-plugin`
    signing
    `maven-publish`
    id("com.gradle.plugin-publish")
//    id("suspend-transform.jvm-maven-publish")
}

//setup(IProject)

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))
    // compileOnly(project(":compiler:suspend-transform-plugin"))
    api(project(":compiler:suspend-transform-plugin-cli"))
    api(project(":compiler:suspend-transform-plugin-configuration"))
    api(project(":compiler:suspend-transform-plugin-deprecated-configuration"))
}

kotlin {
    configGradleBuildSrcFriendly()
    compilerOptions {
        freeCompilerArgs.addAll("-Xjvm-default=all")
    }
}

buildConfig {
    className("SuspendTransPluginConstants")
    useKotlinOutput {
        internalVisibility = false
    }
    packageName("love.forte.plugin.suspendtrans.gradle")

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
if (isMainPublishable()) {
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
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

if (isMainPublishable()) {
    publishing {
        repositories {
            mavenLocal()
            if (project.version.toString().contains("SNAPSHOT", true)) {
                configPublishMaven(SnapshotRepository)
            } else {
                configPublishMaven(ReleaseRepository)
            }
        }

        publications {
//        create<MavenPublication>("GradlePluginMavenPublication") {
//            from(components.getByName("java"))
//        }

            withType<MavenPublication> {

                setupPom(project.name, IProject)
//                pom {
//                    name of project.name
//                    group = project.group
//                    description of project.description
//                    version = project.version.toString()
//                }
                // setupPom(project.name, IProject)
            }
        }
    }
} else {
    tasks.withType<PublishToMavenRepository>().configureEach {
        enabled = false
    }
}


signing {
    val gpgValue = Gpg.ofSystemPropOrNull()
    isRequired = gpgValue != null
    if (gpgValue != null) {
        val (keyId, secretKey, password) = gpgValue
        useInMemoryPgpKeys(keyId, secretKey, password)
        sign(publishingExtension.publications)
    }
}

// TODO see https://github.com/gradle-nexus/publish-plugin/issues/208#issuecomment-1465029831
val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(signingTasks)
}
