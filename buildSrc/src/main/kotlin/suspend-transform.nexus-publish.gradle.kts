import love.forte.gradle.common.core.repository.Repositories
import love.forte.gradle.common.publication.configure.nexusPublishConfig

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

//val versionValue = ""
//
//val versionValue = if (isRelease()) Version.VERSION else "${Version.VERSION}-SNAPSHOT"

//group = "love.forte.plugin.suspend-transform"
//version = "0.0.1"
//description = "Generate platform-compatible functions for Kotlin suspend functions"

//setupWith(libs)

val isPublishConfigurable = isPublishConfigurable()

if (!isPublishConfigurable) {
    logger.warn("sonatype.username or sonatype.password is null, cannot config nexus publishing.")
}

nexusPublishConfig {
    setWithProjectDetail(IProject)
    useStaging = project.provider { !project.version.toString().endsWith("SNAPSHOT", ignoreCase = true) }
    repositoriesConfig = {
        val (sonatypeUsername, sonatypePassword) = sonatypeUserInfoOrNull

        sonatype {
            snapshotRepositoryUrl.set(uri(Repositories.Snapshot.URL))
            username.set(sonatypeUsername)
            password.set(sonatypePassword)
        }
    }
}

logger.info("[nexus-publishing-configure] - [{}] configured.", name)




