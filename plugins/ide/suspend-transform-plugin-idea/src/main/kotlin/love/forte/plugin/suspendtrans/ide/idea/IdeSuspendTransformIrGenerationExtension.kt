package love.forte.plugin.suspendtrans.ide.idea

import love.forte.plugin.suspendtrans.ir.SuspendTransformIrGenerationExtension
import love.forte.plugin.suspendtrans.ir.SuspendTransformTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid


/**
 *
 * @author ForteScarlet
 */
class IdeSuspendTransformIrGenerationExtension : SuspendTransformIrGenerationExtension() {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(SuspendTransformTransformer(pluginContext))
    }
}