package love.forte.plugin.suspendtrans.ide.idea

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement

object SuspendTransformAvailability {
    fun isAvailable(element: PsiElement): Boolean {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return true
        }
        
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return false
        return isAvailable(module)
    }
    
    fun isAvailable(module: Module): Boolean {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return true
        }
        
        return true
        //
        // return SuspendTransformAvailability.PROVIDER_EP.getExtensions(module.project).any { it.isAvailable(module) }
    }
}