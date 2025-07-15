package embeddable

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for creating an embeddable version of a Kotlin compiler plugin.
 */
class EmbeddablePlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.createEmbedded()
        target.afterEvaluate {
            target.jarWithEmbedded()
        }
    }
}
