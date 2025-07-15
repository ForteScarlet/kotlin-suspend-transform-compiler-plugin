package embeddable

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

const val kotlinEmbeddableRootPackage = "org.jetbrains.kotlin"

val packagesToRelocate = listOf(
    "com.intellij",
    "com.google",
    "org.jetbrains",
    "org.apache",
    "org.jdom",
    "org.picocontainer",
    "org.jline",
    "org.fusesource",
    "net.jpountz",
    "one.util.streamex",
    "it.unimi.dsi.fastutil",
    "kotlinx.collections.immutable"
)

fun ConfigurationContainer.getOrCreate(name: String): Configuration = findByName(name) ?: create(name)

private fun ShadowJar.configureEmbeddableCompilerRelocation() {
    relocate("com.google.protobuf", "org.jetbrains.kotlin.protobuf")
    packagesToRelocate.forEach {
        relocate(it, "$kotlinEmbeddableRootPackage.$it")
    }
    relocate("javax.inject", "$kotlinEmbeddableRootPackage.javax.inject")
}

private fun Project.compilerShadowJar(taskName: String, body: ShadowJar.() -> Unit): TaskProvider<out ShadowJar> {
    val embeddedConfig = configurations.getOrCreate("embedded")
    val javaPluginExtension = extensions.getByType<JavaPluginExtension>()

    return tasks.register<ShadowJar>(taskName) {
        group = "shadow"
        destinationDirectory.set(project.file("${layout.buildDirectory.asFile.get()}/libs"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(embeddedConfig)
        from(javaPluginExtension.sourceSets.getByName("main").output)
        body()
    }
}

fun Project.createEmbedded() {
    configurations.create("embedded")
}

fun Project.embeddableCompiler(
    taskName: String = "embeddable",
    body: ShadowJar.() -> Unit = {}
): TaskProvider<out ShadowJar> =
    compilerShadowJar(taskName) {
        configureEmbeddableCompilerRelocation()
        body()
    }

fun Project.jarWithEmbedded() {
    val embeddedTask = embeddableCompiler()
    tasks.named<Jar>("jar").configure {
        actions = emptyList()
        dependsOn(embeddedTask)
    }
}
