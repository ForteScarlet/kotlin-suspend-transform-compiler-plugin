package love.forte.plugin.suspendtrans.ir

import love.forte.plugin.suspendtrans.PluginAvailability
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformIrGenerationExtension : IrGenerationExtension, PluginAvailability {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(SuspendTransformTransformer(pluginContext))
    }
}