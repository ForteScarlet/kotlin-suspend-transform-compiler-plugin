package love.forte.plugin.suspendtrans

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class SuspendTransformComponentRegistrar(
    private val defaultEnabled: Boolean,
) : ComponentRegistrar {
    
    // Used by service loader
    @Suppress("unused")
    constructor() : this(
        defaultEnabled = true,
    )
    
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration,
    ) {
        val enabled = configuration.get(SuspendTransformCommandLineProcessor.ARG_ENABLED, defaultEnabled)
        if (enabled) {
            IrGenerationExtension.registerExtension(project, SuspendTransformIrGenerationExtension())
        }
    }
}

