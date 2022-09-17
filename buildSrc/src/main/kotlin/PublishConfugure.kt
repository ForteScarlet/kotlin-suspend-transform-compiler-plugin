import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
import utils.by
import utils.systemProperty
import java.net.URI

fun RepositoryHandler.configPublishMaven(sonatype: Sonatype, username: String?, password: String?) {
    maven {
        name = sonatype.name
        url = URI(sonatype.url)
        credentials {
            this.username = username
            this.password = password
        }
    }
}

fun MavenPom.setupPom(project: Project) {
    name.set("Kotlin suspend transform compiler plugin - ${project.name}")
    description.set(project.description ?: "")
    url.set("https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin")
    licenses {
        license {
            name by "MIT License"
            url by "https://mit-license.org/"
        }
    }
    scm {
        url.set("https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin")
        connection.set("scm:git:https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin.git")
        developerConnection.set("scm:git:ssh://git@github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin.git")
    }
    setupDevelopers()
}

/**
 * 配置开发者/协作者信息。
 *
 */
fun MavenPom.setupDevelopers() {
    developers {
        developer {
            id.set("forte")
            name.set("ForteScarlet")
            email.set("ForteScarlet@163.com")
            url.set("https://github.com/ForteScarlet")
        }
    }
}



data class SonatypeUserInfo(val sonatypeUsername: String, val sonatypePassword: String)

private val _sonatypeUserInfo: SonatypeUserInfo? by lazy {
    val sonatypeUsername: String? = systemProperty("OSSRH_USER")
    val sonatypePassword: String? = systemProperty("OSSRH_PASSWORD")
    
    if (sonatypeUsername != null && sonatypePassword != null) {
        SonatypeUserInfo(sonatypeUsername, sonatypePassword)
    } else {
        null
    }
}

val sonatypeUserInfo: SonatypeUserInfo get() = _sonatypeUserInfo!!
val sonatypeUserInfoOrNull: SonatypeUserInfo? get() = _sonatypeUserInfo

operator fun SonatypeUserInfo?.component1(): String? = this?.sonatypeUsername
operator fun SonatypeUserInfo?.component2(): String? = this?.sonatypePassword

fun isPublishConfigurable(): Boolean {
    return sonatypeUserInfoOrNull != null
}