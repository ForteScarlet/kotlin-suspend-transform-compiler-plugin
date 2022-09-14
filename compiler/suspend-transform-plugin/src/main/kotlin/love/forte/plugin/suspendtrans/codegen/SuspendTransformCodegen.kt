package love.forte.plugin.suspendtrans.codegen

import love.forte.plugin.suspendtrans.toJvmBlockingAnnotationName
import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
import org.jetbrains.kotlin.codegen.asmType
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Type


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformCodegen(
    private val codegen: ImplementationBodyCodegen
) {
    private val generationState: GenerationState get() = codegen.state
    private inline val v get() = codegen.v
    private inline val clazz get() = codegen.descriptor
    private val typeMapper: KotlinTypeMapper get() = codegen.typeMapper
    fun KotlinType.asmType(): Type = this.asmType(typeMapper)

    fun generate() {
        val members = clazz.unsubstitutedMemberScope
        val names = members.getFunctionNames()
        val functions =
            names.flatMap { members.getContributedFunctions(it, NoLookupLocation.WHEN_GET_ALL_DESCRIPTORS) }.toSet()

        functions.forEach { func ->
            if (func.annotations.hasAnnotation(toJvmBlockingAnnotationName)) {

            }
        }


    }

}