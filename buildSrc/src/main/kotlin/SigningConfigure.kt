import gradle.kotlin.dsl.accessors._59a6e08710bd766406b34ec8c4dcd1fe.signing
import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.plugins.signing.SigningExtension
import utils.systemProperty

fun SigningExtension.setupSigning(publications: DomainObjectCollection<Publication>) {
    val keyId = systemProperty("GPG_KEY_ID")
    val secretKey = systemProperty("GPG_SECRET_KEY")
    val password = systemProperty("GPG_PASSWORD")
    
    if (keyId != null) {
        useInMemoryPgpKeys(keyId, secretKey, password)
    }
    
    setRequired(project.provider { project.gradle.taskGraph.hasTask("publish") })
    sign(publications)
}

internal fun Project.`signing`(configure: Action<SigningExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("signing", configure)
