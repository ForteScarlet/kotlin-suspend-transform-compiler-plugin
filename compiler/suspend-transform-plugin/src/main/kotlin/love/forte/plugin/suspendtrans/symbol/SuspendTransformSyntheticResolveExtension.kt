package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.filterNotCompileAnnotations
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import love.forte.plugin.suspendtrans.utils.resolveAnnotationData
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.descriptorUtil.platform
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.synthetic.isVisibleOutside
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.error.ErrorModuleDescriptor.platform
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
    private val functionAnnotationCache =
        ConcurrentHashMap<SimpleFunctionDescriptor, ConcurrentHashMap<Transformer, TransformAnnotationData>>()

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
            descriptors: AbstractSuspendTransformFunctionDescriptor
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

                fun resolveAnnotationData(transformer: Transformer): TransformAnnotationData? {
                    return functionAnnotationCache.computeIfAbsent(originFunction) { ConcurrentHashMap() }
                        .compute(transformer) { _, current ->
                            current
                                ?: transformer.resolveAnnotationData(
                                    originFunction, originFunction.containingDeclaration, originFunction.name.identifier
                                )
                                ?: originFunction.findSuspendOverridden()?.let { superFunction ->
                                    transformer.resolveAnnotationData(
                                        superFunction,
                                        superFunction.containingDeclaration,
                                        superFunction.name.identifier
                                    )
                                }
                        }
                }

                configuration.transformers.forEach { (targetPlatform, transformers) ->
                    transformers.forEach flb@{ transformer ->
                        val annotationData = resolveAnnotationData(transformer) ?: return@flb
                        generateSyntheticTransformFunction(
                            annotationData,
                            thisDescriptor,
                            originFunction,
                            targetPlatform,
                            transformer
                        )?.also {
                            addSyntheticTransformDescriptors(annotationData, it)
                        }
                    }
                }
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
        targetPlatform: TargetPlatform,
        transformer: Transformer,
    ): AbstractSuspendTransformFunctionDescriptor? {
        if (annotationData == null) return null

        fun check(): Boolean {
            val platform = classDescriptor.platform
            if (platform.isJvm() && targetPlatform == TargetPlatform.JVM) {
                return true
            }

            if (platform.isJs() && targetPlatform == TargetPlatform.JS) {
                return true
            }

            if (platform.isCommon() && targetPlatform == TargetPlatform.COMMON) {
                return true
            }

            return false
        }

        platform.isCommon()

        if (check()) {
            return SimpleSuspendTransformFunctionDescriptor(
                classDescriptor,
                originFunction,
                annotationData.functionName,
                copyAnnotations(originFunction, transformer),
                SuspendTransformUserData(originFunction, asProperty = annotationData.asProperty ?: false, transformer),
                transformer
            ).also { it.init() }
        }

        return null
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
    originFunction: SimpleFunctionDescriptor,
    transformer: Transformer
): Pair<Annotations, Annotations> {

    fun findAnnotation(
        name: ClassId, valueArguments: Map<Name, ConstantValue<*>> = mutableMapOf()
    ): AnnotationDescriptorImpl? {
        val descriptor = originFunction.module.findClassDescriptor(name) ?: return null
        val type = KotlinTypeFactory.simpleNotNullType(TypeAttributes.Empty, descriptor, emptyList())
        return AnnotationDescriptorImpl(type, valueArguments, descriptor.source)
    }

    val (copyFunction, excludes, includes) = CopyAnnotationsData(
        transformer.copyAnnotationsToSyntheticFunction,
        transformer.copyAnnotationExcludes.map { it.toClassId() },
        transformer.syntheticFunctionIncludeAnnotations.map { it.toInfo() }
    )

    val annotationsList = mutableListOf<AnnotationDescriptor>()

    annotationsList.apply {
        if (copyFunction) {
            val notCompileAnnotationsCopied = originFunction.annotations.filterNotCompileAnnotations().filterNot {
                val annotationClassId = it.annotationClass?.classId ?: return@filterNot true
                excludes.any { ex -> annotationClassId == ex }
            }
            addAll(notCompileAnnotationsCopied)
        }

        // try add @Generated(by = ...)
        findAnnotation(
            generatedAnnotationClassId,
            mutableMapOf(Name.identifier("by") to StringArrayValue(originFunction.toGeneratedByDescriptorInfo()))
        )?.also(::add)

        // add includes
        includes.forEach { include ->
            val classId = include.classId
            val unsafeFqName = classId.asSingleFqName().toUnsafe()
            if (!include.repeatable && this.any { it.fqName?.toUnsafe() == unsafeFqName }) {
                return@forEach
            }
            findAnnotation(classId)?.also(::add)
        }
    }

    return Annotations.create(annotationsList) to Annotations.EMPTY
}

private data class CopyAnnotationsData(
    val copyFunction: Boolean,
    val excludes: List<ClassId>,
    val includes: List<IncludeAnnotationInfo>
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

private data class IncludeAnnotationInfo(
    val classId: ClassId,
    val repeatable: Boolean
)

private fun ClassInfo.toClassId(): ClassId {
    return ClassId(packageName.fqn, className.fqn, local)
}

private fun IncludeAnnotation.toInfo(): IncludeAnnotationInfo {
    return IncludeAnnotationInfo(classInfo.toClassId(), repeatable)
}

