package love.forte.plugin.suspendtrans

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.resolve.source.PsiSourceElement

interface PluginAvailability {
    fun PsiElement.isPluginEnabled(): Boolean = true

    fun ModuleDescriptor.isPluginEnabled() = true

    fun ClassDescriptor.isPluginEnabled(): Boolean {
        val sourceElement = (source as? PsiSourceElement)?.psi ?: return false
        return sourceElement.isPluginEnabled()
    }

    fun IrClass.isPluginEnabled(): Boolean {
        val sourceElement = (source as? PsiSourceElement)?.psi ?: return false
        return sourceElement.isPluginEnabled()
    }
}