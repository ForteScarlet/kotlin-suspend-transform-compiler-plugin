package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 *
 * @author ForteScarlet
 */
public class SuspendTransformIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // val typeAnyNullable = pluginContext.irBuiltIns.anyNType
        
        // val debugLogAnnotation = pluginContext.referenceClass(FqName("com.bnorm.debug.log.DebugLog"))!!
        // val funPrintln = pluginContext.referenceFunctions(FqName("kotlin.io.println"))
        //     .single {
        //         val parameters = it.owner.valueParameters
        //         parameters.size == 1 && parameters[0].type == typeAnyNullable
        //     }
        
        moduleFragment.transform(SuspendTransformTransformer(pluginContext), null)
    }
}