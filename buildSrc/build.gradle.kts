plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val kotlinVersion = "1.7.20"
val dokkaPluginVersion = "1.7.20"

dependencies {
    api(gradleApi())
    api(kotlin("gradle-plugin", kotlinVersion))
    api("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaPluginVersion")
    
    api("com.gradle.publish:plugin-publish-plugin:0.12.0")
    api("com.github.gmazzo:gradle-buildconfig-plugin:2.0.2")
    // see https://github.com/bnorm/kotlin-power-assert#compatibility
    api("gradle.plugin.com.bnorm.power:kotlin-power-assert-gradle:0.12.0")
    api("io.github.gradle-nexus:publish-plugin:1.1.0")

    // api("com.github.jengelman.gradle.plugins:shadow:6.1.0")
}


// tasks.test {
//     useJUnitPlatform()
// }
//
// tasks.withType<KotlinCompile> {
//     kotlinOptions.jvmTarget = "1.8"
// }