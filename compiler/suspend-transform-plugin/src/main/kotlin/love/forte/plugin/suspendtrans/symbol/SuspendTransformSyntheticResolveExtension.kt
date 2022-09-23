package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.PluginAvailability
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.generatedAnnotationName
import love.forte.plugin.suspendtrans.utils.*
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.descriptorUtil.*
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.synthetic.isVisibleOutside
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.Variance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue


private class SyntheticDescriptor<D : CallableMemberDescriptor> {
    val descriptors = ConcurrentHashMap<ClassDescriptor, ConcurrentHashMap<String, ConcurrentLinkedQueue<D>>>()
    fun getSyntheticDescriptors(classDescriptor: ClassDescriptor): ConcurrentHashMap<String, ConcurrentLinkedQueue<D>> =
        descriptors.computeIfAbsent(classDescriptor) { ConcurrentHashMap() }

    fun takeSyntheticDescriptors(classDescriptor: ClassDescriptor, name: String): List<D> =
        getSyntheticDescriptors(classDescriptor).remove(name)?.toList() ?: emptyList()

    fun takeSyntheticDescriptors(classDescriptor: ClassDescriptor, name: Name): List<D> =
        takeSyntheticDescriptors(classDescriptor, name.asString())

    fun getCurrentSyntheticDescriptorNames(classDescriptor: ClassDescriptor): List<Name> =
        getSyntheticDescriptors(classDescriptor).keys.map(Name::identifier)

    fun addSyntheticDescriptors(classDescriptor: ClassDescriptor, descriptor: D) {
        getSyntheticDescriptors(classDescriptor).computeIfAbsent(descriptor.name.asString()) { ConcurrentLinkedQueue() }
            .add(descriptor)
    }
}

private enum class SyntheticType {
    JVM_BLOCKING, JVM_ASYNC, JS_ASYNC
}

private val FunctionDescriptor.allParametersExpectDispatch: List<ParameterDescriptor>
    get() {
        val vps = valueParameters
        val cps = contextReceiverParameters
        return buildList(vps.size + cps.size + 1) {
            extensionReceiverParameter?.let { add(it) }
            addAll(cps)
            addAll(vps)
        }
    }

/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformSyntheticResolveExtension(open val configuration: SuspendTransformConfiguration) :
    SyntheticResolveExtension, PluginAvailability {
    private val functionAnnotationsCache = ConcurrentHashMap<SimpleFunctionDescriptor, FunctionTransformAnnotations>()

    private val syntheticFunctions = SyntheticDescriptor<SimpleFunctionDescriptor>()
    private val syntheticProperties = SyntheticDescriptor<PropertyDescriptor>()

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> {
        if (!thisDescriptor.isPluginEnabled()) {
            return super.getSyntheticFunctionNames(thisDescriptor)
        }
        return syntheticFunctions.getCurrentSyntheticDescriptorNames(thisDescriptor)
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

        fun addSyntheticTransformDescriptors(
            annotationData: TransformAnnotationData?,
            descriptors: AbstractSuspendTransformFunctionDescriptor<*>
        ) {
            if (annotationData == null) return

            if (annotationData.asProperty == true && descriptors.valueParameters.isEmpty()) {
                syntheticProperties.addSyntheticDescriptors(
                    thisDescriptor,
                    descriptors.transformToProperty(annotationData)
                )
            } else {
                syntheticFunctions.addSyntheticDescriptors(thisDescriptor, descriptors)
            }
        }

        fun FunctionDescriptor.findSuspendOverridden(): SimpleFunctionDescriptor? {
            return fromSupertypes.find { sup ->
                if (!sup.isSuspend) return@find false
                val thisAllParams = this.allParametersExpectDispatch
                val supAllParams = sup.allParametersExpectDispatch

                if (thisAllParams.size != supAllParams.size) return@find false

                thisAllParams.forEachIndexed { index, p ->
                    val supp = supAllParams[index]

                    if (p.type != supp.type) {
                        return@find false
                    }
                }

                true
            }
        }

        // check and add synthetic functions.
        // find all annotated
        result
            .asSequence()
            .filter { f -> f.isSuspend }
            .filter { f -> f.visibility.isVisibleOutside() }
            .forEach { originFunction ->
                var resolvedAnnotations =
                    functionAnnotationsCache.computeIfAbsent(originFunction) { f ->
                        f.resolveToTransformAnnotations(
                            thisDescriptor,
                            configuration
                        )
                    }
                if (resolvedAnnotations.isEmpty) {
                    println("ORIGINAL: $originFunction")
                    println(originFunction.overriddenTreeUniqueAsSequence(true).toList())
                    println(originFunction.overriddenTreeUniqueAsSequence(false).toList())
                    println(originFunction.overriddenTreeAsSequence(true).toList())
                    println(originFunction.overriddenTreeAsSequence(false).toList())
                    originFunction.original
                    println()
                    // find from overridden function
                    val superAnnotation = originFunction.findSuspendOverridden()?.let { superFunction ->
                        functionAnnotationsCache.computeIfAbsent(superFunction) { f ->
                            f.resolveToTransformAnnotations(
                                configuration = configuration
                            )
                        }
                    }?.resolveByFunctionInheritable()
                        ?.takeIf { !it.isEmpty }
                        ?: return@forEach

                    resolvedAnnotations = superAnnotation
                }

                generateSyntheticTransformFunction(
                    resolvedAnnotations.jvmBlockingAnnotationData,
                    thisDescriptor,
                    originFunction,
                    SyntheticType.JVM_BLOCKING
                )?.also { addSyntheticTransformDescriptors(resolvedAnnotations.jvmBlockingAnnotationData, it) }

                generateSyntheticTransformFunction(
                    resolvedAnnotations.jvmAsyncAnnotationData,
                    thisDescriptor,
                    originFunction,
                    SyntheticType.JVM_ASYNC
                )?.also { addSyntheticTransformDescriptors(resolvedAnnotations.jvmAsyncAnnotationData, it) }

                generateSyntheticTransformFunction(
                    resolvedAnnotations.jsAsyncAnnotationData,
                    thisDescriptor,
                    originFunction,
                    SyntheticType.JS_ASYNC
                )?.also { addSyntheticTransformDescriptors(resolvedAnnotations.jsAsyncAnnotationData, it) }
            }

        // get synthetic functions, add into result
        syntheticFunctions.takeSyntheticDescriptors(thisDescriptor, name).forEach {
            result += it
        }

    }


    private fun generateSyntheticTransformFunction(
        annotationData: TransformAnnotationData?,
        classDescriptor: ClassDescriptor,
        originFunction: SimpleFunctionDescriptor,
        type: SyntheticType,
    ): AbstractSuspendTransformFunctionDescriptor<*>? {
        if (annotationData == null) return null
        return when {
            classDescriptor.platform.isJvm() && type == SyntheticType.JVM_BLOCKING -> SuspendTransformJvmBlockingFunctionDescriptorImpl(
                classDescriptor,
                originFunction,
                annotationData.functionName,
                copyAnnotations(configuration, originFunction, type),
            )

            classDescriptor.platform.isJvm() && type == SyntheticType.JVM_ASYNC -> SuspendTransformJvmAsyncFunctionDescriptorImpl(
                classDescriptor,
                originFunction,
                annotationData.functionName,
                copyAnnotations(configuration, originFunction, type),
            )

            classDescriptor.platform.isJs() && type == SyntheticType.JS_ASYNC -> SuspendTransformJsPromiseFunctionImpl(
                classDescriptor,
                originFunction,
                annotationData.functionName,
                copyAnnotations(configuration, originFunction, type),
            )

            else -> null
        }.also { it?.init() }
    }


    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {
        if (!thisDescriptor.isPluginEnabled()) {
            return super.getSyntheticPropertiesNames(thisDescriptor)
        }

        return syntheticProperties.getCurrentSyntheticDescriptorNames(thisDescriptor)
    }

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {
        syntheticProperties.takeSyntheticDescriptors(thisDescriptor, name).forEach {
            result += it
        }
    }


}

private fun copyAnnotations(
    configuration: SuspendTransformConfiguration,
    originFunction: SimpleFunctionDescriptor,
    syntheticType: SyntheticType
): Pair<Annotations, Annotations> {

    fun findAnnotation(
        name: FqName, valueArguments: Map<Name, ConstantValue<*>> = mutableMapOf()
    ): AnnotationDescriptorImpl? {
        val descriptor = originFunction.module.findClassDescriptor(name) ?: return null
        val type = KotlinTypeFactory.simpleNotNullType(TypeAttributes.Empty, descriptor, emptyList())
        return AnnotationDescriptorImpl(type, valueArguments, descriptor.source)
    }

    val (copyFunction, excludes, includes) = when (syntheticType) {
        SyntheticType.JVM_BLOCKING -> {
            configuration.jvm.let {
                CopyAnnotationsData(
                    it.copyAnnotationsToSyntheticBlockingFunction,
                    it.copyAnnotationsToSyntheticBlockingFunctionExcludes,
                    it.syntheticBlockingFunctionIncludeAnnotations
                )
            }
        }

        SyntheticType.JVM_ASYNC -> {
            configuration.jvm.let {
                CopyAnnotationsData(
                    it.copyAnnotationsToSyntheticAsyncFunction,
                    it.copyAnnotationsToSyntheticAsyncFunctionExcludes,
                    it.syntheticAsyncFunctionIncludeAnnotations
                )
            }
        }

        SyntheticType.JS_ASYNC -> {
            configuration.js.let {
                CopyAnnotationsData(
                    it.copyAnnotationsToSyntheticAsyncFunction,
                    it.copyAnnotationsToSyntheticAsyncFunctionExcludes,
                    it.syntheticAsyncFunctionIncludeAnnotations
                )
            }
        }
    }

    val annotationsList = mutableListOf<AnnotationDescriptor>()
//    val propertyAnnotationsList = if (property) mutableListOf<AnnotationDescriptor>() else null
//
//    fun addAnnotation(annotationDescriptor: AnnotationDescriptor) {
//        annotationsList.add(annotationDescriptor)
//        annotationDescriptor
////        if (propertyAnnotationsList != null) {
////            annotationDescriptor.annotationClass?.annotations?.findAnnotation(ANNOTATION)
////        }
//    }

    annotationsList.apply {
        if (copyFunction) {
            val notCompileAnnotationsCopied = originFunction.annotations.filterNotCompileAnnotations().filterNot {
                val annotationFqNameUnsafe = it.annotationClass?.fqNameUnsafe ?: return@filterNot true
                excludes.any { ex -> annotationFqNameUnsafe == ex.name.fqn.toUnsafe() }
            }
            addAll(notCompileAnnotationsCopied)
        }

        // add @Generated(by = ...)
        findAnnotation(
            generatedAnnotationName,
            mutableMapOf(Name.identifier("by") to StringArrayValue(originFunction.toGeneratedByDescriptorInfo()))
        )?.also(::add)

        // add includes
        includes.forEach { include ->
            val name = include.name.fqn
            val unsafeFqName = name.toUnsafe()
            if (!include.repeatable && this.any { it.fqName?.toUnsafe() == unsafeFqName }) {
                return@forEach
            }
            findAnnotation(name)?.also(::add)
        }
    }

    return Annotations.create(annotationsList) to Annotations.EMPTY
}

private data class CopyAnnotationsData(
    val copyFunction: Boolean,
    val excludes: List<SuspendTransformConfiguration.ExcludeAnnotation>,
    val includes: List<SuspendTransformConfiguration.IncludeAnnotation>
)

private class StringArrayValue(values: List<StringValue>) : ArrayValue(values, { module ->
    module.builtIns.getArrayType(
        Variance.INVARIANT, module.builtIns.stringType
    )
})

private val String.sv: StringValue get() = StringValue(this)
private val Name.sv: StringValue get() = StringValue(asString())

private fun SimpleFunctionDescriptor.toGeneratedByDescriptorInfo(): List<StringValue> {
    val d = this
    return buildList {
        add(d.name.sv)
        d.allParameters.forEach {
            add(it.name.sv)
            add(it.type.getJetTypeFqName(true).sv)
        }
        add(d.returnType?.getJetTypeFqName(true)?.sv ?: "?".sv)
    }

}