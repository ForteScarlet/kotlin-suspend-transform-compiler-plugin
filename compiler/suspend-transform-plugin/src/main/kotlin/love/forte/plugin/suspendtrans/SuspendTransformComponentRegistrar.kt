package love.forte.plugin.suspendtrans

import com.google.auto.service.AutoService
import com.intellij.mock.MockProject
import love.forte.plugin.suspendtrans.ir.SuspendTransformIrGenerationExtension
import love.forte.plugin.suspendtrans.symbol.SuspendTransformSyntheticResolveExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@AutoService(ComponentRegistrar::class)
class SuspendTransformComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration,
    ) {

        val suspendTransformConfiguration = configuration.resolveToSuspendTransformConfiguration()

        if (suspendTransformConfiguration.enabled) {
            SyntheticResolveExtension.registerExtension(project, SuspendTransformSyntheticResolveExtension(suspendTransformConfiguration))
            IrGenerationExtension.registerExtension(project, SuspendTransformIrGenerationExtension(suspendTransformConfiguration))
        }
    }
}


private fun CompilerConfiguration.resolveToSuspendTransformConfiguration(): SuspendTransformConfiguration {
//    val compilerConfiguration = this
    return get(SuspendTransformCommandLineProcessor.CONFIGURATION, SuspendTransformConfiguration())
//    return SuspendTransformConfiguration().apply {
//        enabled = compilerConfiguration.get(SuspendTransformCommandLineProcessor.ENABLED, true)
//    }
}