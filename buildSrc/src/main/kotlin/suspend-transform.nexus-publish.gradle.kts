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




