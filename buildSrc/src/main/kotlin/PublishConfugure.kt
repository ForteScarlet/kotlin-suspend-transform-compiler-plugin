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

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
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

//fun MavenPom.setupPom(project: Project) {
//    name.set("Kotlin suspend transform compiler plugin - ${project.name}")
//    description.set(project.description ?: "")
//    url.set("https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin")
//    licenses {
//        license {
//            name by "MIT License"
//            url by "https://mit-license.org/"
//        }
//    }
//    scm {
//        url.set("https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin")
//        connection.set("scm:git:https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin.git")
//        developerConnection.set("scm:git:ssh://git@github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin.git")
//    }
//    setupDevelopers()
//}

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
