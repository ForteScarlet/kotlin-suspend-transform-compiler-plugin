import gradle.kotlin.dsl.accessors._59a6e08710bd766406b34ec8c4dcd1fe.publishing
import gradle.kotlin.dsl.accessors._59a6e08710bd766406b34ec8c4dcd1fe.signing
import utils.systemProperty
import utils.by

plugins {
    id("org.jetbrains.dokka")
    signing
    `maven-publish`
}


val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

val sonatypeContains = sonatypeUserInfoOrNull != null

val jarJavadoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // from(tasks.findByName("dokkaHtml"))
}

publishing {
    publications {
        configureEach {
            if (this !is MavenPublication) {
                return@configureEach
            }
            
            artifact(jarJavadoc)
            
            pom {
                setupPom(project)
            }
        }
        
        repositories {
            mavenLocal()
            if (sonatypeContains) {
                if (project.version.toString().contains("SNAPSHOT", true)) {
                    configPublishMaven(Sonatype.Snapshot, sonatypeUsername, sonatypePassword)
                } else {
                    configPublishMaven(Sonatype.Central, sonatypeUsername, sonatypePassword)
                }
            }
        }
    }
}


signing {
    setupSigning(publishing.publications)
}


inline val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer

inline val Project.publishing: PublishingExtension
    get() = extensions.getByType<PublishingExtension>()


fun Project.`kotlin`(configure: Action<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>): Unit =
    (this as ExtensionAware).extensions.configure("kotlin", configure)

fun MavenPublication.jar(taskName: String, config: Action<Jar>) = artifact(tasks.create(taskName, Jar::class, config))

fun MavenPublication.javadocJar(taskName: String, config: Jar.() -> Unit = {}) = jar(taskName) {
    archiveClassifier by "javadoc"
    config()
}