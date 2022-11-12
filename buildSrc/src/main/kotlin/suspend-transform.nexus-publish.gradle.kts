import utils.by

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

//val versionValue = ""
//
//val versionValue = if (isRelease()) Version.VERSION else "${Version.VERSION}-SNAPSHOT"

group = "love.forte.plugin.suspend-transform"
version = "0.0.1"
description = "Generate platform-compatible functions for Kotlin suspend functions"

val isPublishConfigurable = isPublishConfigurable()

if (!isPublishConfigurable) {
    logger.warn("sonatype.username or sonatype.password is null, cannot config nexus publishing.")
}


if (isPublishConfigurable) {
    val (sonatypeUsername, sonatypePassword) = sonatypeUserInfo
    nexusPublishing {
        logger.info("[NEXUS] - project.group:   ${project.group}")
        logger.info("[NEXUS] - project.version: ${project.version}")
        packageGroup by project.group.toString()
        repositoryDescription by (project.description ?: "")
        
        useStaging.set(
            project.provider { !project.version.toString().endsWith("SNAPSHOT", ignoreCase = true) }
        )
        
        clientTimeout by 30 unit TimeUnit.MINUTES
        connectTimeout by 30 unit TimeUnit.MINUTES
        
        
        transitionCheckOptions {
            maxRetries by 150
            delayBetween by 15 unit TimeUnit.SECONDS
        }
        
        repositories {
            sonatype {
                snapshotRepositoryUrl by uri(Sonatype.Snapshot.URL)
                username by sonatypeUsername
                password by sonatypePassword
            }
        }
    }
    
    
    logger.info("[nexus-publishing-configure] - [{}] configured.", name)
}




