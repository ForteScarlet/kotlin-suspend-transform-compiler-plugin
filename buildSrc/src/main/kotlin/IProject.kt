import love.forte.gradle.common.core.project.ProjectDetail
import love.forte.gradle.common.core.project.Version
import love.forte.gradle.common.core.project.version

object IProject : ProjectDetail() {

    const val GROUP = "love.forte.plugin.suspend-transform"
    const val DESCRIPTION = "Generate platform-compatible functions for Kotlin suspend functions"
    const val HOMEPAGE = "https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin"

    override val version: Version = version(0, 6, 0)

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
