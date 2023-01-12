package love.forte.plugin.suspendtrans

import com.google.auto.service.AutoService
import love.forte.plugin.suspendtrans.ir.SuspendTransformIrGenerationExtension
import love.forte.plugin.suspendtrans.symbol.SuspendTransformSyntheticResolveExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class SuspendTransformComponentRegistrar : CompilerPluginRegistrar() {

    //internal var defaultConfiguration: SuspendTransformConfiguration? = null

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val suspendTransformConfiguration =/* defaultConfiguration ?: */
            configuration.resolveToSuspendTransformConfiguration()

        val suspendTransformSyntheticResolveExtension =
            SuspendTransformSyntheticResolveExtension(suspendTransformConfiguration)

        val suspendTransformIrGenerationExtension = SuspendTransformIrGenerationExtension(suspendTransformConfiguration)

        SyntheticResolveExtension.registerExtension(suspendTransformSyntheticResolveExtension)
        IrGenerationExtension.registerExtension(suspendTransformIrGenerationExtension)
    }



//    override fun registerProjectComponents(
//        project: MockProject,
//        configuration: CompilerConfiguration,
//    ) {
//
//        val suspendTransformConfiguration =/* defaultConfiguration ?: */configuration.resolveToSuspendTransformConfiguration()
//
//        if (suspendTransformConfiguration.enabled) {
//            SyntheticResolveExtension.registerExtension(project, SuspendTransformSyntheticResolveExtension(suspendTransformConfiguration))
//            IrGenerationExtension.registerExtension(project, SuspendTransformIrGenerationExtension(suspendTransformConfiguration))
//        }
//    }
}


private fun CompilerConfiguration.resolveToSuspendTransformConfiguration(): SuspendTransformConfiguration {
//    val compilerConfiguration = this
    return get(SuspendTransformCommandLineProcessor.CONFIGURATION, SuspendTransformConfiguration())
//    return SuspendTransformConfiguration().apply {
//        enabled = compilerConfiguration.get(SuspendTransformCommandLineProcessor.ENABLED, true)
//    }
}
