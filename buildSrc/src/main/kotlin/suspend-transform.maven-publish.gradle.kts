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

import com.vanniktech.maven.publish.SonatypeHost
import love.forte.gradle.common.core.property.ofIf

plugins {
    signing
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

val p = project

// check sign
val signRequired = System.getenv("SIGNING_KEY_ID") != null

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (signRequired) {
        signAllPublications()
    }
    coordinates(groupId = p.group.toString(), artifactId = p.name, version = p.version.toString())

    pom {
        name = p.name
        description = p.description
        url = IProject.HOMEPAGE
        licenses {
            IProject.licenses.forEach { license ->
                license {
                    name ofIf license.name
                    url ofIf license.url
                    distribution ofIf license.distribution
                    comments ofIf license.comments
                }
            }
        }

        val scm = IProject.scm
        scm {
            url ofIf scm.url
            connection ofIf scm.connection
            developerConnection ofIf scm.developerConnection
            tag ofIf scm.tag
        }

        developers {
            IProject.developers.forEach { developer ->
                developer {
                    id ofIf developer.id
                    name ofIf developer.name
                    email ofIf developer.email
                    url ofIf developer.url
                    organization ofIf developer.organization
                    organizationUrl ofIf developer.organizationUrl
                    timezone ofIf developer.timezone
                    roles.addAll(developer.roles)
                    properties.putAll(developer.properties)
                }
            }
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues")
        }
    }
}
