package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.utils.*
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
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

            if (annotationData.asProperty && descriptors.valueParameters.isEmpty()) {
                syntheticProperties.addSyntheticDescriptors(
                    thisDescriptor,
                    descriptors.transformToProperty(annotationData)
                )
            } else {
                syntheticFunctions.addSyntheticDescriptors(thisDescriptor, descriptors)
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
                            // 不检测'继承'的注解
                            // ?: originFunction.findSuspendOverridden()?.let { superFunction ->
                            //     transformer.resolveAnnotationData(
                            //         superFunction,
                            //         superFunction.containingDeclaration,
                            //         superFunction.name.identifier
                            //     )
                            // }
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

            return when {
                platform.isJvm() && targetPlatform == TargetPlatform.JVM -> true
                platform.isJs() && targetPlatform == TargetPlatform.JS -> true
                platform.isWasm() && targetPlatform == TargetPlatform.WASM -> true
                platform.isNative() && targetPlatform == TargetPlatform.NATIVE -> true
                platform.isCommon() && targetPlatform == TargetPlatform.COMMON -> true
                else -> false
            }
        }

        if (check()) {
            return SimpleSuspendTransformFunctionDescriptor(
                classDescriptor,
                originFunction,
                annotationData.functionName,
                copyAnnotations(originFunction, transformer),
                SuspendTransformUserData(originFunction, asProperty = annotationData.asProperty, transformer),
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

/**
 * annotations to property annotations
 */
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

    val (copyFunction, copyProperty, excludes, includes) = CopyAnnotationsData(
        transformer.copyAnnotationsToSyntheticFunction,
        transformer.copyAnnotationsToSyntheticProperty,
        transformer.copyAnnotationExcludes.map { it.toClassId() },
        transformer.syntheticFunctionIncludeAnnotations.map { it.toInfo() }
    )

    val functionAnnotationsList = buildList<AnnotationDescriptor> {
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

    val propertyAnnotationsList = buildList<AnnotationDescriptor> {
        if (copyProperty) {
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
        includes
            .filter { it.includeProperty }
            .forEach { include ->
                val classId = include.classId
                val unsafeFqName = classId.asSingleFqName().toUnsafe()
                if (!include.repeatable && this.any { it.fqName?.toUnsafe() == unsafeFqName }) {
                    return@forEach
                }
                findAnnotation(classId)?.also(::add)
            }
    }

    return Annotations.create(functionAnnotationsList) to Annotations.create(propertyAnnotationsList)
}

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
//            add(it.type.getJetTypeFqName(true).sv)
            add(it.type.getKotlinTypeFqName(true).sv)
        }
        add(d.returnType?.getKotlinTypeFqName(true)?.sv ?: "?".sv)
    }

}
