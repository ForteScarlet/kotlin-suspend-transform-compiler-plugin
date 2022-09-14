package love.forte.plugin.suspendtrans.codegen

import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformCodegenExtension : ExpressionCodegenExtension {
    override fun generateClassSyntheticParts(codegen: ImplementationBodyCodegen) {
        println("generateClassSyntheticParts(codegen), codegen = $codegen")
        SuspendTransformCodegen(codegen).generate()
    }

    override val shouldGenerateClassSyntheticPartsInLightClassesMode: Boolean
        get() = true

}