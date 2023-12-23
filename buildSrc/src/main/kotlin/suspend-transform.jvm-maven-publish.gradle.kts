import love.forte.gradle.common.core.Gpg
import love.forte.gradle.common.core.project.setup
import love.forte.gradle.common.publication.configure.jvmConfigPublishing
import love.forte.gradle.common.publication.configure.publishingExtension
import utils.isMainPublishable

plugins {
    id("signing")
    id("maven-publish")
}

setup(IProject)

//val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

//val sonatypeContains = sonatypeUserInfoOrNull != null
val gpgValue = Gpg.ofSystemPropOrNull()

// see https://github.com/gradle/gradle/issues/26091#issuecomment-1681343496

val p = project

if (isMainPublishable()) {
    jvmConfigPublishing {
        project = IProject
        isSnapshot = project.version.toString().contains("SNAPSHOT", true)

        val jarSources = tasks.register("${p.name}SourceJar", Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        val jarJavadoc = tasks.register("${p.name}JavadocJar", Jar::class) {
//            dependsOn(tasks.dokkaHtml)
//            from(tasks.dokkaHtml.flatMap { it.outputDirectory })
            archiveClassifier.set("javadoc")
        }

        tasks.withType<GenerateModuleMetadata> {
            dependsOn(jarSources)
            dependsOn(jarJavadoc)
        }

        artifact(jarSources)
        artifact(jarJavadoc)

        releasesRepository = ReleaseRepository
        snapshotRepository = SnapshotRepository

        gpg = gpgValue
    }

}


signing {
    isRequired = gpgValue != null
    if (gpgValue != null) {
        val (keyId, secretKey, password) = gpgValue
        useInMemoryPgpKeys(keyId, secretKey, password)
        sign(publishingExtension.publications)
    }
}

// TODO see https://github.com/gradle-nexus/publish-plugin/issues/208#issuecomment-1465029831
val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(signingTasks)
}
// see https://github.com/gradle/gradle/issues/26091#issuecomment-1722947958
//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://github.com/gradle/gradle/issues/26091
//tasks.withType<AbstractPublishToMaven>().configureEach {
//    val signingTasks = tasks.withType<Sign>()
//    mustRunAfter(signingTasks)
//}
//endregion


inline val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer

internal val TaskContainer.dokkaHtml: TaskProvider<org.jetbrains.dokka.gradle.DokkaTask>
    get() = named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml")
