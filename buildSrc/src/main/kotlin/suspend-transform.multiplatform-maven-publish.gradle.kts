import love.forte.gradle.common.core.Gpg
import love.forte.gradle.common.core.project.setup
import love.forte.gradle.common.core.property.systemProp
import love.forte.gradle.common.publication.configure.multiplatformConfigPublishing

plugins {
    id("org.jetbrains.dokka")
    signing
    `maven-publish`
}

setup(IProject)

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}


val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

val sonatypeContains = sonatypeUserInfoOrNull != null

val p = project
multiplatformConfigPublishing {
    project = IProject

    val jarJavadoc by tasks.registering(Jar::class) {
        group = "documentation"
        archiveClassifier.set("javadoc")
        from(tasks.findByName("dokkaHtml"))
    }
    artifact(jarJavadoc)
    isSnapshot = project.version.toString().contains("SNAPSHOT", true)
    releasesRepository = ReleaseRepository
    snapshotRepository = SnapshotRepository
    gpg = Gpg.ofSystemPropOrNull()

    if (systemProp("SIMBOT_LOCAL").toBoolean()) {
        logger.info("Is 'SIMBOT_LOCAL', mainHost set as null")
        mainHost = null
    }

}
