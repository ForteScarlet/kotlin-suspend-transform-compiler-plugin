package love.forte.plugin.suspendtrans.ide.idea

import love.forte.plugin.suspendtrans.symbol.SuspendTransformSyntheticResolveExtension


/**
 *
 * @author ForteScarlet
 */
class IdeSuspendTransformResolveExtension : SuspendTransformSyntheticResolveExtension() {
    override fun com.intellij.psi.PsiElement.isPluginEnabled(): Boolean {
        return SuspendTransformAvailability.isAvailable(this)
    }
}