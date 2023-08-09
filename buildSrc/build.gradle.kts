plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val kotlinVersion = "1.9.0"
val dokkaPluginVersion = "1.8.20"
val gradleCommon = "0.0.11"

dependencies {
    api(gradleApi())
    api(kotlin("gradle-plugin", kotlinVersion))
    api(kotlin("serialization", kotlinVersion))
    api("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaPluginVersion")
    
    api("com.gradle.publish:plugin-publish-plugin:0.12.0")
    api("com.github.gmazzo:gradle-buildconfig-plugin:3.1.0")
    // see https://github.com/bnorm/kotlin-power-assert#compatibility
    api("gradle.plugin.com.bnorm.power:kotlin-power-assert-gradle:0.12.0")
    api("io.github.gradle-nexus:publish-plugin:1.1.0")

     api("com.github.jengelman.gradle.plugins:shadow:6.1.0")

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
