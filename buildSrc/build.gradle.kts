plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val kotlinVersion: String = libs.versions.kotlin.get()
val dokkaPluginVersion = "1.9.20"
val gradleCommon = "0.6.0"
val nexusPublishPlugin = "2.0.0"
val buildConfig = "5.3.5"

dependencies {
    api(gradleApi())
    api(kotlin("gradle-plugin", kotlinVersion))
    api(kotlin("serialization", kotlinVersion))
    api("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaPluginVersion")

    // see https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html
    api("com.gradle.publish:plugin-publish-plugin:1.2.1")
    // see https://github.com/gmazzo/gradle-buildconfig-plugin
    api("com.github.gmazzo.buildconfig:plugin:$buildConfig")
    // see https://github.com/gradle-nexus/publish-plugin
    api("io.github.gradle-nexus:publish-plugin:$nexusPublishPlugin")

    // see https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html#configure-the-project
    // see https://github.com/vanniktech/gradle-maven-publish-plugin
    // see https://plugins.gradle.org/plugin/com.vanniktech.maven.publish
    implementation(libs.maven.publish)

//    api("com.github.jengelman.gradle.plugins:shadow:8.3.0")
    // https://gradleup.com/shadow/
    api("com.gradleup.shadow:shadow-gradle-plugin:8.3.2")

    implementation("love.forte.gradle.common:gradle-common-core:$gradleCommon")
    implementation("love.forte.gradle.common:gradle-common-kotlin-multiplatform:$gradleCommon")
    implementation("love.forte.gradle.common:gradle-common-publication:$gradleCommon")
}

// tasks.test {
//     useJUnitPlatform()
// }
//
// tasks.withType<KotlinCompile> {
//     kotlinOptions.jvmTarget = "1.8"
// }
