/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import love.forte.gradle.common.core.Gpg
import love.forte.gradle.common.publication.configure.configPublishMaven
import love.forte.gradle.common.publication.configure.publishingExtension
import love.forte.gradle.common.publication.configure.setupPom
import love.forte.gradle.common.publication.configure.signingExtension
import utils.isMainPublishable

plugins {
    id("signing")
    id("maven-publish")
}

//setup(IProject)

//val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

//val sonatypeContains = sonatypeUserInfoOrNull != null
val gpgValue = Gpg.ofSystemPropOrNull()

// see https://github.com/gradle/gradle/issues/26091#issuecomment-1681343496

val p = project

if (isMainPublishable()) {
    val isSnapshot = project.version.toString().contains("SNAPSHOT")
    publishingExtension {
        repositories {
            mavenLocal()
            if (isSnapshot) {
                configPublishMaven(SnapshotRepository)
            } else {
                configPublishMaven(ReleaseRepository)
            }
        }
        val jarSources = tasks.register("${p.name}SourceJar", Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        val jarJavadoc = tasks.register("${p.name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
        }

        publications {
            create<MavenPublication>("publicationDist") {
                from(components.getByName("java"))
                artifact(jarSources)
                artifact(jarJavadoc)
                version = p.version.toString()
                setupPom(p.name, IProject)
            }
        }

        signingExtension {
            val gpg = gpgValue ?: return@signingExtension

            val (keyId, secretKey, password) = gpg
            useInMemoryPgpKeys(keyId, secretKey, password)
            sign(publishingExtension.publications)
        }
    }
//    jvmConfigPublishing {
//        project = IProject
//        isSnapshot = project.version.toString().contains("SNAPSHOT", true)
//
//        val jarSources = tasks.register("${p.name}SourceJar", Jar::class) {
//            archiveClassifier.set("sources")
//            from(sourceSets["main"].allSource)
//        }
//
//        val jarJavadoc = tasks.register("${p.name}JavadocJar", Jar::class) {
////            dependsOn(tasks.dokkaHtml)
////            from(tasks.dokkaHtml.flatMap { it.outputDirectory })
//            archiveClassifier.set("javadoc")
//        }
//
//        tasks.withType<GenerateModuleMetadata> {
//            dependsOn(jarSources)
//            dependsOn(jarJavadoc)
//        }
//
//        artifact(jarSources)
//        artifact(jarJavadoc)
//
//        releasesRepository = ReleaseRepository
//        snapshotRepository = SnapshotRepository
//
//        gpg = gpgValue
//    }

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
