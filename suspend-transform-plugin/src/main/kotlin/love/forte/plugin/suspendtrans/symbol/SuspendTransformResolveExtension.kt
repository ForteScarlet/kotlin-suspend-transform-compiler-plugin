package love.forte.plugin.suspendtrans.symbol

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformResolveExtension : SyntheticResolveExtension /*ExpressionCodegenExtension*/ {
    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        val collector = mutableListOf<FunctionDescriptor>()
        // val memberScope = thisDescriptor.getMemberScope(TypeSubstitution.EMPTY)
        // println(memberScope)
        // memberScope.getDescriptorsFiltered().forEach {
        //     println(it)
        // }
        // TODO func?
        //FunctionDescriptor
        val members = thisDescriptor.unsubstitutedMemberScope
        println("members: $members")
        println("members.getClassifierNames: ${members.getClassifierNames()}")
        println("members.getVariableNames: ${members.getVariableNames()}")
        println("members.getFunctionNames: ${members.getFunctionNames()}")
        val names = members.getFunctionNames()
        val functions =
            names.flatMap { members.getContributedFunctions(it, NoLookupLocation.WHEN_GET_ALL_DESCRIPTORS) }.toSet()

        functions.forEach {
            println("func: ${it.name}")
        }

        return emptyList()
        // return collector.flatMap { func ->
        //     val names = mutableListOf<String>()
        //     val annotations = func.annotations
        //     if (func.platform?.isJvm() == true) {
        //         // @JvmBlocking
        //         val blockingAnnotation = annotations.findAnnotation(toJvmBlockingAnnotationName)
        //         if (blockingAnnotation != null) {
        //             val blockingFunctionName = blockingAnnotation.functionName(
        //                 defaultBaseName = func.name.identifier,
        //                 defaultSuffix = "Blocking"
        //             )
        //             names.add(blockingFunctionName)
        //         }
        //
        //
        //         // @JvmAsync
        //         val asyncAnnotation = annotations.findAnnotation(toJvmAsyncAnnotationName)
        //         if (asyncAnnotation != null) {
        //             val asyncFunctionName =
        //                 asyncAnnotation.functionName(defaultBaseName = func.name.identifier, defaultSuffix = "Async")
        //             names.add(asyncFunctionName)
        //         }
        //     }
        //
        //     if (func.platform?.isJs() == true) {
        //         // @JsPromise
        //         val jsAnnotation = annotations.findAnnotation(toJsPromiseAnnotationName)
        //         if (jsAnnotation != null) {
        //             val functionName =
        //                 jsAnnotation.functionName(defaultBaseName = func.name.identifier, defaultSuffix = "Async")
        //             names.add(functionName)
        //         }
        //
        //     }
        //
        //     println("Names: $names")
        //     names
        // }.map { Name.identifier(it) }
    }
    
}

private class FunctionCollector<C : MutableCollection<FunctionDescriptor>>(
    val collector: C,
) : DeclarationDescriptorVisitor<Void?, Void?> {
    override fun visitFunctionDescriptor(descriptor: FunctionDescriptor?, data: Void?): Void? {
        if (descriptor != null) {
            collector.add(descriptor)
        }
        return null
    }
    
    override fun visitPackageFragmentDescriptor(descriptor: PackageFragmentDescriptor?, data: Void?): Void? = null
    
    override fun visitPackageViewDescriptor(descriptor: PackageViewDescriptor?, data: Void?): Void? = null
    
    override fun visitVariableDescriptor(descriptor: VariableDescriptor?, data: Void?): Void? = null
    
    
    override fun visitTypeParameterDescriptor(descriptor: TypeParameterDescriptor?, data: Void?): Void? = null
    
    
    override fun visitClassDescriptor(descriptor: ClassDescriptor?, data: Void?): Void? = null
    
    
    override fun visitTypeAliasDescriptor(descriptor: TypeAliasDescriptor?, data: Void?): Void? = null
    
    
    override fun visitModuleDeclaration(descriptor: ModuleDescriptor?, data: Void?): Void? = null
    
    
    override fun visitConstructorDescriptor(constructorDescriptor: ConstructorDescriptor?, data: Void?): Void? = null
    
    override fun visitScriptDescriptor(scriptDescriptor: ScriptDescriptor?, data: Void?): Void? = null
    
    override fun visitPropertyDescriptor(descriptor: PropertyDescriptor?, data: Void?): Void? = null
    
    override fun visitValueParameterDescriptor(descriptor: ValueParameterDescriptor?, data: Void?): Void? = null
    
    
    override fun visitPropertyGetterDescriptor(descriptor: PropertyGetterDescriptor?, data: Void?): Void? = null
    
    
    override fun visitPropertySetterDescriptor(descriptor: PropertySetterDescriptor?, data: Void?): Void? = null
    
    
    override fun visitReceiverParameterDescriptor(descriptor: ReceiverParameterDescriptor?, data: Void?): Void? = null
    
}