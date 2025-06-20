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
