plugins {
    java
    id("suspend-transform.jvm-maven-publish")
    // https://github.com/bennyhuo/kotlin-compiler-plugin-embeddable-plugin
    id("com.bennyhuo.kotlin.plugin.embeddable") version "1.7.10.0"
}

dependencies {
    embedded(project(":suspend-transform-plugin"))
}