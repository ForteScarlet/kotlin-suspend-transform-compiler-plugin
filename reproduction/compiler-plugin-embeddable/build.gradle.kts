plugins {
    kotlin("jvm") version "2.0.0"
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish-local")
}

apply<embeddable.EmbeddablePlugin>()

dependencies {
    add("embedded", project(":compiler-plugin"))
}
