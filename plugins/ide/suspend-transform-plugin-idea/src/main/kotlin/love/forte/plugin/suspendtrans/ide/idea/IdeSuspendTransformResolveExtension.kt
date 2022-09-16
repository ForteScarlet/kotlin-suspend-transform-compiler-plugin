package love.forte.plugin.suspendtrans.ide.idea

import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.symbol.SuspendTransformSyntheticResolveExtension


/**
 *
 * @author ForteScarlet
 */
class IdeSuspendTransformResolveExtension : SuspendTransformSyntheticResolveExtension(SuspendTransformConfiguration()) {
    override fun com.intellij.psi.PsiElement.isPluginEnabled(): Boolean {
        return configuration.enabled && SuspendTransformAvailability.isAvailable(this)
    }
}