import embeddable.EmbeddablePlugin

plugins {
    java
    id("suspend-transform.jvm-maven-publish")
    // https://github.com/bennyhuo/kotlin-compiler-plugin-embeddable-plugin
//    id("com.bennyhuo.kotlin.plugin.embeddable") version "1.7.10.0"
}

apply<EmbeddablePlugin>()

dependencies {
    add("embedded", project(":compiler:suspend-transform-plugin"))
//    embedded(project(":compiler:suspend-transform-plugin"))
}
