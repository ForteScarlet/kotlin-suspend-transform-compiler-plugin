package love.forte.plugin.suspendtrans.ir

import love.forte.plugin.suspendtrans.PluginAvailability
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformIrGenerationExtension(private val configuration: SuspendTransformConfiguration) :
    IrGenerationExtension, PluginAvailability {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(SuspendTransformTransformer(configuration, pluginContext))
    }
}
