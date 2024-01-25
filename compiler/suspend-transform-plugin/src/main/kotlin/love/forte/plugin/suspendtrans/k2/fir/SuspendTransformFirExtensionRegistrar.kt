package love.forte.plugin.suspendtrans.k2.fir

import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformFirExtensionRegistrar(private val suspendTransformConfiguration: SuspendTransformConfiguration) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        FirDeclarationGenerationExtension.Factory { session -> SuspendTransformFirTransformer(session, suspendTransformConfiguration) }.unaryPlus()
    }
}
