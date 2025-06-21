import embeddable.EmbeddablePlugin

plugins {
    java
    kotlin("jvm")
    id("suspend-transform.maven-publish")
    id("com.gradleup.shadow")
}

apply<EmbeddablePlugin>()

dependencies {
    add("embedded", project(":compiler:suspend-transform-plugin"))
//    embedded(project(":compiler:suspend-transform-plugin"))
}
