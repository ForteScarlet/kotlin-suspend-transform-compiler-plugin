plugins {
    id("org.jetbrains.dokka")
    signing
    `maven-publish`
}

// val dokkaJar by tasks.creating(Jar::class) {
//     group = DOCUMENTATION_GROUP
//     description = "Assembles Kotlin docs with Dokka"
//     archiveClassifier.set("javadoc")
//     from(tasks["dokkaHtml"])
// }


val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

val sonatypeContains = sonatypeUserInfoOrNull != null

val jarJavadoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // from(tasks.findByName("dokkaHtml"))
}

val jarSources by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("suspendTransformJvmDist") {
            from(components["java"])
            artifact(jarSources)
            artifact(jarJavadoc)

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }

        configureEach {
            if (this is MavenPublication) {
                pom {
                    setupPom(project)
                }
            }
        }

        repositories {
            mavenCentral()
            if (sonatypeContains) {
                if (project.version.toString().contains("SNAPSHOT", true)) {
                    configPublishMaven(Sonatype.Snapshot, sonatypeUsername, sonatypePassword)
                } else {
                    configPublishMaven(Sonatype.Central, sonatypeUsername, sonatypePassword)
                }
            }
            mavenLocal()
        }
    }
}


signing {
    setupSigning(publishing.publications)
}


inline val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer
