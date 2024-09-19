import embeddable.EmbeddablePlugin

plugins {
    java
    kotlin("jvm")
    id("suspend-transform.jvm-maven-publish")
    id("com.gradleup.shadow")
    // https://github.com/bennyhuo/kotlin-compiler-plugin-embeddable-plugin
//    id("com.bennyhuo.kotlin.plugin.embeddable") version "1.7.10.0"
}

apply<EmbeddablePlugin>()

dependencies {
    add("embedded", project(":compiler:suspend-transform-plugin")) { isTransitive = false }
//    embedded(project(":compiler:suspend-transform-plugin"))
}
