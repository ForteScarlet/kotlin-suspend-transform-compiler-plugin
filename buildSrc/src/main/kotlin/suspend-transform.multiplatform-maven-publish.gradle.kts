// import love.forte.gradle.common.core.Gpg
// import love.forte.gradle.common.publication.configure.configPublishMaven
// import love.forte.gradle.common.publication.configure.publishingExtension
// import love.forte.gradle.common.publication.configure.setupPom
//
// plugins {
//     id("org.jetbrains.dokka")
//     signing
//     `maven-publish`
// }
//
// tasks.withType<JavaCompile> {
//     sourceCompatibility = "1.8"
//     targetCompatibility = "1.8"
//     options.encoding = "UTF-8"
// }
//
// val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull
// val sonatypeContains = sonatypeUserInfoOrNull != null
//
// val p = project
// val isSnapshot = p.version.toString().contains("SNAPSHOT", true)
//
// val jarJavadoc by tasks.registering(Jar::class) {
//     group = "documentation"
//     archiveClassifier.set("javadoc")
// }
//
// publishing {
//     repositories {
//         mavenLocal()
//         if (isSnapshot) {
//             configPublishMaven(SnapshotRepository)
//         } else {
//             configPublishMaven(ReleaseRepository)
//         }
//     }
//
//     publications {
//         withType<MavenPublication> {
//             artifacts {
//                 artifact(jarJavadoc)
//             }
//
//             setupPom(project.name, IProject)
//         }
//     }
// }
//
// signing {
//     val gpg = Gpg.ofSystemPropOrNull() ?: return@signing
//     val (keyId, secretKey, password) = gpg
//     useInMemoryPgpKeys(keyId, secretKey, password)
//     sign(publishingExtension.publications)
// }
//
// //multiplatformConfigPublishing {
// //    project = IProject
// //
// //    val jarJavadoc by tasks.registering(Jar::class) {
// //        group = "documentation"
// //        archiveClassifier.set("javadoc")
// //    }
// //    artifact(jarJavadoc)
// //    releasesRepository = ReleaseRepository
// //    snapshotRepository = SnapshotRepository
// //    gpg = Gpg.ofSystemPropOrNull()
// //
// //    if (systemProp("SIMBOT_LOCAL").toBoolean()) {
// //        logger.info("Is 'SIMBOT_LOCAL', mainHost set as null")
// //        mainHost = null
// //    }
// //
// //    publicationsFromMainHost += setOf("wasm", "wasm32", "wasm_js")
// //    mainHostSupportedTargets += setOf("wasm", "wasm32", "wasm_js")
// //}
//
// // TODO see https://github.com/gradle-nexus/publish-plugin/issues/208#issuecomment-1465029831
// val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
// tasks.withType<PublishToMavenRepository>().configureEach {
//     mustRunAfter(signingTasks)
// }
