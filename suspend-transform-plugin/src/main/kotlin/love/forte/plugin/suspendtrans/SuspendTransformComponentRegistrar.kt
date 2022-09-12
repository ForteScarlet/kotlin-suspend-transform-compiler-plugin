package love.forte.plugin.suspendtrans

import com.google.auto.service.AutoService
import love.forte.plugin.suspendtrans.symbol.SuspendTransformResolveExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

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
            SyntheticResolveExtension.registerExtension(project, SuspendTransformResolveExtension())
            IrGenerationExtension.registerExtension(project, SuspendTransformIrGenerationExtension())
        }
    }
}

