package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.PluginAvailability
import love.forte.plugin.suspendtrans.generatedAnnotationName
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.filterNotCompileAnnotations
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import love.forte.plugin.suspendtrans.utils.resolveToTransformAnnotations
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.JvmNames
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.descriptorUtil.platform
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

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

    // class<Name, functions>
    private val syntheticFunctions =
        ConcurrentHashMap<ClassDescriptor, ConcurrentHashMap<String, ConcurrentLinkedQueue<SimpleFunctionDescriptor>>>()

    private val ClassDescriptor.syntheticFunctions: ConcurrentHashMap<String, ConcurrentLinkedQueue<SimpleFunctionDescriptor>>
        get() = this@SuspendTransformSyntheticResolveExtension.syntheticFunctions.computeIfAbsent(this) { ConcurrentHashMap() }

    private fun takeSyntheticFunctions(
        classDescriptor: ClassDescriptor,
        functionName: String
    ): List<SimpleFunctionDescriptor> {
        val functions = classDescriptor.syntheticFunctions.remove(functionName)

        return functions?.toList() ?: emptyList()
    }

    private fun getCurrentSyntheticFunctionNames(classDescriptor: ClassDescriptor): List<Name> {
        return classDescriptor.syntheticFunctions.keys.map(Name::identifier)
    }

    private fun addSyntheticFunctions(classDescriptor: ClassDescriptor, function: SimpleFunctionDescriptor) {
        classDescriptor.syntheticFunctions.computeIfAbsent(function.name.identifier) { ConcurrentLinkedQueue() }
            .add(function)
    }

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        if (!thisDescriptor.isPluginEnabled()) {
            return super.getSyntheticFunctionNames(thisDescriptor)
        }
        return getCurrentSyntheticFunctionNames(thisDescriptor).also {
            println("descriptor = $thisDescriptor, names = $it")
        }
//        val currentSyntheticData = thisDescriptor.currentSyntheticData
//
//
//        val names = mutableListOf<Name>()
//        val syntheticFunctions = currentSyntheticData.syntheticFunctionData
//
//        // find all annotated
//        currentSyntheticData.normalFunctions.forEach { originFunction ->
//            val resolvedAnnotations =
//                originFunction.annotations.resolveToTransformAnnotations(originFunction.name.identifier)
//            if (resolvedAnnotations.isEmpty) {
//                return@forEach
//            }
//
//
//            fun resolveByAnnotationData(annotationData: TransformAnnotationData?, type: SyntheticType) {
//                if (annotationData != null) {
//                    val functionName = annotationData.functionName
//                    val functionNameName = Name.identifier(functionName)
//                    names.add(functionNameName)
//                    println("thisDescriptor.name = ${thisDescriptor.name} functionName = $functionName, originFunction = $originFunction, type = $type")
//                    syntheticFunctions
//                        .computeIfAbsent(functionName) { mutableListOf() }
//                        .add(
//                            SyntheticFunctionData(
//                                functionNameName,
//                                originFunction,
//                                annotationData,
//                                type
//                            ).also {
//                                println(it)
//                            }
//                        )
//                }
//            }
//            if (thisDescriptor.platform?.isJvm() == true) {
//                resolveByAnnotationData(resolvedAnnotations.jvmBlockingAnnotationData, SyntheticType.JVM_BLOCKING)
//                resolveByAnnotationData(resolvedAnnotations.jvmAsyncAnnotationData, SyntheticType.JVM_ASYNC)
//            }
//            if (thisDescriptor.platform?.isJs() == true) {
//                resolveByAnnotationData(resolvedAnnotations.jsAsyncAnnotationData, SyntheticType.JS_ASYNC)
//            }
//        }
//
//        currentSyntheticData.inGenerating = true
//        currentSyntheticData.normalFunctions.clear()
//        return names
    }

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>,
    ) {
        if (!thisDescriptor.isPluginEnabled()) {
            return super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
        }


//        val currentSyntheticData = thisDescriptor.currentSyntheticData
//        if (!currentSyntheticData.inGenerating) {
//            currentSyntheticData.normalFunctions.addAll(result)
//            return super.generateSyntheticMethods(thisDescriptor, name, bindingContext, fromSupertypes, result)
//        }

        // check and add synthetic functions.
        // find all annotated
        result.forEach { originFunction ->
            val resolvedAnnotations =
                originFunction.annotations.resolveToTransformAnnotations(originFunction.name.identifier)
            if (resolvedAnnotations.isEmpty) {
                return@forEach
            }

            if (thisDescriptor.platform?.isJvm() == true) {
                resolvedAnnotations.jvmBlockingAnnotationData?.let { jvmBlocking ->
                    addSyntheticFunctions(thisDescriptor, SuspendTransformJvmBlockingFunctionDescriptorImpl(
                        thisDescriptor,
                        originFunction,
                        Name.identifier(jvmBlocking.functionName),
                        thisDescriptor.copyAnnotations(originFunction)
                    ).apply {
                        init()
                    })
                }

                resolvedAnnotations.jvmAsyncAnnotationData?.let { jvmAsync ->
                    addSyntheticFunctions(thisDescriptor, SuspendTransformJvmAsyncFunctionDescriptorImpl(
                        thisDescriptor,
                        originFunction,
                        Name.identifier(jvmAsync.functionName),
                        thisDescriptor.copyAnnotations(originFunction)
                    ).apply {
                        init()
                    })
                }
            }

            if (thisDescriptor.platform?.isJs() == true) {
                resolvedAnnotations.jsAsyncAnnotationData?.let { jsAsync ->
                    addSyntheticFunctions(thisDescriptor, SuspendTransformJsPromiseFunctionImpl(
                        thisDescriptor,
                        originFunction,
                        Name.identifier(jsAsync.functionName),
                        thisDescriptor.copyAnnotations(originFunction)
                    ).apply {
                        init()
                    })
                }
            }
        }

        // get synthetic functions, add into result
        takeSyntheticFunctions(thisDescriptor, name.identifier).forEach {
            result += it
        }

    }

}

private fun ClassDescriptor.copyAnnotations(originFunction: SimpleFunctionDescriptor): Annotations {

    val notCompileAnnotationsCopied = this.annotations.filterNotCompileAnnotations()

    fun findAnnotation(name: FqName): AnnotationDescriptorImpl {
        val descriptor = requireNotNull(originFunction.module.findClassDescriptor(name))
        val type = KotlinTypeFactory.simpleNotNullType(TypeAttributes.Empty, descriptor, emptyList())
        return AnnotationDescriptorImpl(type, mutableMapOf(), descriptor.source)
    }

    // add @Generated
    val generatedDescriptor = requireNotNull(originFunction.module.findClassDescriptor(generatedAnnotationName))
    val generatedAnnotationKotlinType =
        KotlinTypeFactory.simpleNotNullType(TypeAttributes.Empty, generatedDescriptor, emptyList())


    return Annotations.create(
        buildList {
            addAll(notCompileAnnotationsCopied)
            add(findAnnotation(generatedAnnotationName))
            if (platform?.isJvm() == true && kind.isInterface) {
                // add @JvmDefault in jvm..?
                add(findAnnotation(JvmNames.JVM_DEFAULT_FQ_NAME))
            }
        }
    )
}