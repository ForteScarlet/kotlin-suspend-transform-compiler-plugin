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

import IProject.IS_SNAPSHOT
import love.forte.gradle.common.core.project.ProjectDetail
import org.gradle.api.Project

object IProject : ProjectDetail() {
    const val IS_SNAPSHOT = false

    const val GROUP = "love.forte.plugin.suspend-transform"
    const val DESCRIPTION = "Generate platform-compatible functions for Kotlin suspend functions"
    const val HOMEPAGE = "https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin"

    // Remember the libs.versions.toml!
    val ktVersion = "2.2.20"
    val pluginVersion = "0.13.2"

    override val version: String = "$ktVersion-$pluginVersion"

    override val homepage: String get() = HOMEPAGE

    override val description: String get() = DESCRIPTION

    override val developers: List<Developer> = developers {
        developer {
            id = "forte"
            name = "ForteScarlet"
            email = "ForteScarlet@163.com"
            url = "https://github.com/ForteScarlet"
        }
    }
    override val group: String get() = GROUP

    override val licenses: List<License> = licenses {
        license {
            name = "MIT License"
            url = "https://mit-license.org/"
        }
    }
    override val scm: Scm = scm {
        url = HOMEPAGE
        connection = "scm:git:$HOMEPAGE.git"
        developerConnection = "scm:git:ssh://git@github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin.git"
    }

}

fun Project.setupWith(ktVersion: String) {
    group = IProject.GROUP
    description = IProject.DESCRIPTION
    val mergedVersion = ktVersion + "-" + IProject.pluginVersion
    version = if (IS_SNAPSHOT) "$mergedVersion-SNAPSHOT" else mergedVersion
}
