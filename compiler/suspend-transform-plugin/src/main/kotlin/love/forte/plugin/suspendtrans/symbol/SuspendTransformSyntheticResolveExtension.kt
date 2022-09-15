package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.PluginAvailability
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.resolveToTransformAnnotations
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.platform
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import java.util.concurrent.ConcurrentHashMap

private data class SyntheticData(
    val classDescriptor: ClassDescriptor,
    var inGenerating: Boolean = false,
    val normalFunctions: MutableList<SimpleFunctionDescriptor> = mutableListOf(),
    val syntheticFunctionData: MutableMap<String, MutableList<SyntheticFunctionData>> = mutableMapOf(),
    val syntheticPropertyData: MutableMap<String, MutableList<SyntheticPropertyData>> = mutableMapOf(),
)

private data class SyntheticFunctionData(
    val name: Name,
    val originFunctionDescriptor: SimpleFunctionDescriptor,
    val annotationData: TransformAnnotationData,
    val type: SyntheticType,
)

private data class SyntheticPropertyData(
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
    private val syntheticDataMap = ConcurrentHashMap<ClassDescriptor, SyntheticData>()

    private val ClassDescriptor.currentSyntheticData: SyntheticData
        get() = syntheticDataMap.computeIfAbsent(this, ::SyntheticData)

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        if (!thisDescriptor.isPluginEnabled()) {
            return super.getSyntheticFunctionNames(thisDescriptor)
        }


        val currentSyntheticData = thisDescriptor.currentSyntheticData


        val names = mutableListOf<Name>()
        val syntheticFunctions = currentSyntheticData.syntheticFunctionData

        // find all annotated
        currentSyntheticData.normalFunctions.forEach { originFunction ->
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
                    println("thisDescriptor.name = ${thisDescriptor.name} functionName = $functionName, originFunction = $originFunction, type = $type")
                    syntheticFunctions
                        .computeIfAbsent(functionName) { mutableListOf() }
                        .add(
                            SyntheticFunctionData(
                                functionNameName,
                                originFunction,
                                annotationData,
                                type
                            ).also {
                                println(it)
                            }
                        )
                }
            }
            if (thisDescriptor.platform?.isJvm() == true) {
                resolveByAnnotationData(resolvedAnnotations.jvmBlockingAnnotationData, SyntheticType.JVM_BLOCKING)
                resolveByAnnotationData(resolvedAnnotations.jvmAsyncAnnotationData, SyntheticType.JVM_ASYNC)
            }
            if (thisDescriptor.platform?.isJs() == true) {
                resolveByAnnotationData(resolvedAnnotations.jsAsyncAnnotationData, SyntheticType.JS_ASYNC)
            }
        }

        currentSyntheticData.inGenerating = true
        currentSyntheticData.normalFunctions.clear()
        return names
    }

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>,
    ) {
        println("thisDescriptor    = $thisDescriptor")
        println("name              = $name")
        println("bindingContext    = $bindingContext")
        println("fromSupertypes    = $fromSupertypes")
        println("result            = $result")
        println("========")
        if (!thisDescriptor.isPluginEnabled()) {
            return super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
        }


        val currentSyntheticData = thisDescriptor.currentSyntheticData
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
                        name,
                        thisDescriptor.copyAnnotations(originFunctionDescriptor)
                    ).apply {
                        init()
                    }
                }

                SyntheticType.JVM_ASYNC -> {
                    result += SuspendTransformJvmAsyncFunctionDescriptorImpl(
                        thisDescriptor,
                        originFunctionDescriptor,
                        name,
                        thisDescriptor.copyAnnotations(originFunctionDescriptor)
                    ).apply {
                        init()
                    }
                }

                SyntheticType.JS_ASYNC -> {
                    result += SuspendTransformJsPromiseFunctionImpl(
                        thisDescriptor,
                        originFunctionDescriptor,
                        name,
                        thisDescriptor.copyAnnotations(originFunctionDescriptor)
                    ).apply {
                        init()
                    }
                }
            }
        }

        //currentSyntheticData.inGenerating = false
    }

}

private fun ClassDescriptor.copyAnnotations(originFunction: SimpleFunctionDescriptor): Annotations {
    if (platform?.isJvm() == true && kind.isInterface) {
        // add @JvmDefault in jvm
    }

    return Annotations.EMPTY
}