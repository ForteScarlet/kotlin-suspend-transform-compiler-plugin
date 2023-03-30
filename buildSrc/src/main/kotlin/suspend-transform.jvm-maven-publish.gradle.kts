import love.forte.gradle.common.core.Gpg
import love.forte.gradle.common.core.project.setup
import love.forte.gradle.common.publication.configure.jvmConfigPublishing
import love.forte.gradle.common.publication.configure.setupPom

plugins {
    id("org.jetbrains.dokka")
    signing
    `maven-publish`
}

setup(IProject)

//val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

//val sonatypeContains = sonatypeUserInfoOrNull != null

jvmConfigPublishing {
    project = IProject
    val jarSources by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val jarJavadoc by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    artifact(jarSources)
    artifact(jarJavadoc)

    isSnapshot = project.version.toString().contains("SNAPSHOT", true)
    releasesRepository = ReleaseRepository
    snapshotRepository = SnapshotRepository
    gpg = Gpg.ofSystemPropOrNull()

}

publishing.publications.configureEach {
    if (this is MavenPublication) {
        pom {
            setupPom(project.name, IProject)
        }
    }
}

//publishing {
//    publications {
//        create<MavenPublication>("suspendTransformJvmDist") {
//            from(components["java"])
//            artifact(jarSources)
//            artifact(jarJavadoc)
//
//            groupId = project.group.toString()
//            artifactId = project.name
//            version = project.version.toString()
//        }
//
//        configureEach {
//            if (this is MavenPublication) {
//                pom {
//                    setupPom(project)
//                }
//            }
//        }
//
//        repositories {
//            mavenCentral()
//            if (sonatypeContains) {
//                if (project.version.toString().contains("SNAPSHOT", true)) {
//                    configPublishMaven(Sonatype.Snapshot, sonatypeUsername, sonatypePassword)
//                } else {
//                    configPublishMaven(Sonatype.Central, sonatypeUsername, sonatypePassword)
//                }
//            }
//            mavenLocal()
//        }
//    }
//}
//signing {
//    setupSigning(publishing.publications)
//}


inline val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer
