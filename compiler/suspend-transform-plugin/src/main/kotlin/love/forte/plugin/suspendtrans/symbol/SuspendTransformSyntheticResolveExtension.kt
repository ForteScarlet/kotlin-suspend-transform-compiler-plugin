package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.PluginAvailability
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.generatedAnnotationName
import love.forte.plugin.suspendtrans.utils.filterNotCompileAnnotations
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import love.forte.plugin.suspendtrans.utils.resolveToTransformAnnotations
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
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
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.descriptorUtil.platform
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
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

/**
 *
 * @author ForteScarlet
 */
open class SuspendTransformSyntheticResolveExtension(val configuration: SuspendTransformConfiguration) :
    SyntheticResolveExtension, PluginAvailability {

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


        // check and add synthetic functions.
        // find all annotated
        result.forEach { originFunction ->
            val resolvedAnnotations =
                originFunction.annotations.resolveToTransformAnnotations(configuration, originFunction.name.identifier)
            if (resolvedAnnotations.isEmpty) {
                return@forEach
            }

            if (thisDescriptor.platform?.isJvm() == true) {
                resolvedAnnotations.jvmBlockingAnnotationData?.let { jvmBlocking ->
                    syntheticFunctions.addSyntheticDescriptors(thisDescriptor,
                        SuspendTransformJvmBlockingFunctionDescriptorImpl(
                            thisDescriptor,
                            originFunction,
                            jvmBlocking.functionName,
                            thisDescriptor.copyAnnotations(configuration, originFunction, SyntheticType.JVM_BLOCKING)
                        ).apply {
                            init()
                        })
                }

                resolvedAnnotations.jvmAsyncAnnotationData?.let { jvmAsync ->
                    syntheticFunctions.addSyntheticDescriptors(thisDescriptor,
                        SuspendTransformJvmAsyncFunctionDescriptorImpl(
                            thisDescriptor,
                            originFunction,
                            jvmAsync.functionName,
                            thisDescriptor.copyAnnotations(configuration, originFunction, SyntheticType.JVM_ASYNC)
                        ).apply {
                            init()
                        })
                }
            }

            if (thisDescriptor.platform?.isJs() == true) {
                resolvedAnnotations.jsAsyncAnnotationData?.let { jsAsync ->
                    syntheticFunctions.addSyntheticDescriptors(thisDescriptor, SuspendTransformJsPromiseFunctionImpl(
                        thisDescriptor,
                        originFunction,
                        jsAsync.functionName,
                        thisDescriptor.copyAnnotations(configuration, originFunction, SyntheticType.JS_ASYNC)
                    ).apply {
                        init()
                    })
                }
            }
        }

        // get synthetic functions, add into result
        syntheticFunctions.takeSyntheticDescriptors(thisDescriptor, name).forEach {
            result += it
        }

    }


    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {
        if (!thisDescriptor.isPluginEnabled()) {
            return super.getSyntheticPropertiesNames(thisDescriptor)
        }

        return syntheticProperties.getCurrentSyntheticDescriptorNames(thisDescriptor)
    }


}

private fun ClassDescriptor.copyAnnotations(
    configuration: SuspendTransformConfiguration, originFunction: SimpleFunctionDescriptor, syntheticType: SyntheticType
): Annotations {

    fun findAnnotation(
        name: FqName, valueArguments: Map<Name, ConstantValue<*>> = mutableMapOf()
    ): AnnotationDescriptorImpl? {
        val descriptor = originFunction.module.findClassDescriptor(name) ?: return null
        val type = KotlinTypeFactory.simpleNotNullType(TypeAttributes.Empty, descriptor, emptyList())
        return AnnotationDescriptorImpl(type, valueArguments, descriptor.source)
    }

    val (copy, excludes, includes) = when (syntheticType) {
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


    return Annotations.create(buildList {
        if (copy) {
            val notCompileAnnotationsCopied = this@copyAnnotations.annotations.filterNotCompileAnnotations().filterNot {
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


        // -Xjvm-default=all
//            if (platform?.isJvm() == true && kind.isInterface) {
//                // add @JvmDefault in jvm..?
//                add(findAnnotation(JvmNames.JVM_DEFAULT_FQ_NAME))
//            }
    })
}

private data class CopyAnnotationsData(
    val copy: Boolean,
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