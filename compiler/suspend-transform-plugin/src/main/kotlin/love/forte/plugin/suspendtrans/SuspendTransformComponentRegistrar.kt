package love.forte.plugin.suspendtrans

import love.forte.plugin.suspendtrans.fir.SuspendTransformFirExtensionRegistrar
import love.forte.plugin.suspendtrans.ir.SuspendTransformIrGenerationExtension
import love.forte.plugin.suspendtrans.symbol.SuspendTransformSyntheticResolveExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@OptIn(ExperimentalCompilerApi::class)
class SuspendTransformComponentRegistrar : CompilerPluginRegistrar() {

    //internal var defaultConfiguration: SuspendTransformConfiguration? = null

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        register(this, configuration)
    }


    companion object {
        fun register(storage: ExtensionStorage, configuration: CompilerConfiguration) {
            val suspendTransformConfiguration =/* defaultConfiguration ?: */
                configuration.resolveToSuspendTransformConfiguration()

            register(storage, suspendTransformConfiguration)
        }

        fun register(storage: ExtensionStorage, configuration: SuspendTransformConfiguration) {
            val suspendTransformSyntheticResolveExtension =
                SuspendTransformSyntheticResolveExtension(configuration)
            val suspendTransformFirExtensionRegistrar =
                SuspendTransformFirExtensionRegistrar(configuration)

            val suspendTransformIrGenerationExtension =
                SuspendTransformIrGenerationExtension(configuration)

            with(storage) {
                SyntheticResolveExtension.registerExtension(suspendTransformSyntheticResolveExtension)
                FirExtensionRegistrarAdapter.registerExtension(suspendTransformFirExtensionRegistrar)
                IrGenerationExtension.registerExtension(suspendTransformIrGenerationExtension)
            }
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
