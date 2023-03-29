package embeddable

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by benny.
 */
class EmbeddablePlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.createEmbedded()
        target.afterEvaluate {
            target.jarWithEmbedded()
        }
    }
}
