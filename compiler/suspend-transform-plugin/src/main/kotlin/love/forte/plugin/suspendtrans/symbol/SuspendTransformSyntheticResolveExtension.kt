package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.PluginAvailability
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.resolveToTransformAnnotations
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

private data class CurrentSyntheticData(
    val classDescriptor: ClassDescriptor,
    var inGenerating: Boolean = false,
    val normalFunctions: MutableList<SimpleFunctionDescriptor> = mutableListOf(),
    val syntheticFunctionData: MutableMap<String, MutableList<SyntheticFunctionData>> = mutableMapOf(),
)

private data class SyntheticFunctionData(
    val name: Name,
    val originFunctionDescriptor: SimpleFunctionDescriptor,
    val annotationData: TransformAnnotationData,
    val type: SyntheticType,
)

private enum class SyntheticType {
    JVM_BLOCKING, JVM_ASYNC, JS_ASYNC
}


/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformSyntheticResolveExtension : SyntheticResolveExtension, PluginAvailability {
    private var currentSyntheticData: CurrentSyntheticData? = null

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        if(!thisDescriptor.isPluginEnabled()) {
            return super.getSyntheticFunctionNames(thisDescriptor)
        }

        if (currentSyntheticData?.classDescriptor != thisDescriptor) {
            return emptyList()
        }
        
        
        val names = mutableListOf<Name>()
        val syntheticFunctions = currentSyntheticData?.syntheticFunctionData
        
        // find all annotated
        currentSyntheticData?.normalFunctions?.forEach { originFunction ->
            val resolvedAnnotations =
                originFunction.annotations.resolveToTransformAnnotations(originFunction.name.identifier)
            if (resolvedAnnotations.isEmpty) {
                return@forEach
            }
            
            fun resolveByAnnotationData(annotationData: TransformAnnotationData?, type: SyntheticType) {
                if (annotationData != null) {
                    val functionName = annotationData.functionName
                    val functionNameName = Name.identifier(functionName)
                    names.add(functionNameName)
                    syntheticFunctions
                        ?.computeIfAbsent(functionName) { mutableListOf() }
                        ?.add(
                            SyntheticFunctionData(
                                functionNameName,
                                originFunction,
                                annotationData,
                                type
                            )
                        )
                }
            }
            
            resolveByAnnotationData(resolvedAnnotations.jvmBlockingAnnotationData, SyntheticType.JVM_BLOCKING)
            resolveByAnnotationData(resolvedAnnotations.jvmAsyncAnnotationData, SyntheticType.JVM_ASYNC)
            
        }
        
        
        currentSyntheticData?.inGenerating = true
        return names
    }
    
    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>,
    ) {
        if(!thisDescriptor.isPluginEnabled()) {
            return
        }

        if (currentSyntheticData?.classDescriptor != thisDescriptor) {
            // reset it.
            currentSyntheticData = CurrentSyntheticData(thisDescriptor)
        }
        val currentSyntheticData = this.currentSyntheticData as CurrentSyntheticData
        if (!currentSyntheticData.inGenerating) {
            currentSyntheticData.normalFunctions.addAll(result)
            return super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
        }
        
        // in generate
        currentSyntheticData.syntheticFunctionData[name.identifier]?.forEach { (name, originFunctionDescriptor, _, type) ->
            when (type) {
                SyntheticType.JVM_BLOCKING -> {
                    result += SuspendTransformJvmBlockingFunctionDescriptorImpl(
                        thisDescriptor,
                        originFunctionDescriptor,
                        name
                    ).apply {
                        init()
                    }
                }
                
                SyntheticType.JVM_ASYNC -> {
                    result += SuspendTransformJvmAsyncFunctionDescriptorImpl(
                        thisDescriptor,
                        originFunctionDescriptor,
                        name
                    ).apply {
                        init()
                    }
                }
                
                SyntheticType.JS_ASYNC -> {
                    result += SuspendTransformJsPromiseFunctionImpl(
                        thisDescriptor,
                        originFunctionDescriptor,
                        name
                    ).apply {
                        init()
                    }
                }
            }
        }
        
        // result.mapNotNull { func ->
        //     val resolvedAnnotations = func.annotations.resolveToTransformAnnotations(func.name.identifier)
        //     if (resolvedAnnotations.isEmpty) {
        //         null
        //     } else {
        //         func to resolvedAnnotations
        //     }
        // }.forEach { (func, resolvedAnnotations) ->
        //     println("func = $func, resolvedAnnotations = $resolvedAnnotations")
        //
        //     val jvmBlocking = resolvedAnnotations.jvmBlockingAnnotationData
        //     if (jvmBlocking != null) {
        //         result += SuspendTransformJvmBlockingFunctionDescriptorImpl(
        //             thisDescriptor, jvmBlocking.functionName,
        //         ).apply {
        //             init(func)
        //         }
        //     }
        //
        //     val jsAsync = resolvedAnnotations.jsAsyncAnnotationData
        //     if (jsAsync != null) {
        //
        //     }
        //
        // }
    }
    
}