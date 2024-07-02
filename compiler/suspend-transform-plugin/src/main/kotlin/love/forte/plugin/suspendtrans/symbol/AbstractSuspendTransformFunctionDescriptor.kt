package love.forte.plugin.suspendtrans.symbol

import love.forte.plugin.suspendtrans.SuspendTransformUserData
import love.forte.plugin.suspendtrans.SuspendTransformUserDataKey
import love.forte.plugin.suspendtrans.Transformer
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.copy
import love.forte.plugin.suspendtrans.utils.findClassDescriptor
import love.forte.plugin.suspendtrans.utils.toClassId
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isArrayOrNullableArray
import org.jetbrains.kotlin.types.typeUtil.isNothing
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import org.jetbrains.kotlin.types.typeUtil.isUnit


/**
 *
 * @author ForteScarlet
 */
sealed class AbstractSuspendTransformFunctionDescriptor(
    private val classDescriptor: ClassDescriptor,
    private val originFunction: SimpleFunctionDescriptor,
    functionName: Name,
    annotations: Annotations,
    val propertyAnnotations: Annotations,
    private val userData: SuspendTransformUserData,
    val transformer: Transformer
) : SimpleFunctionDescriptorImpl(
    classDescriptor,
    null,
    annotations,
    functionName,
    CallableMemberDescriptor.Kind.DECLARATION,
    originFunction.source
) {

    protected open fun returnType(originReturnType: KotlinType?): KotlinType? {
        val returnType = transformer.transformReturnType ?: return originReturnType

        val returnTypeClass = requireNotNull(classDescriptor.module.findClassDescriptor(returnType.toClassId())) {
            "Unable to find classDescriptor ${returnType.toClassId()}"
        }

        val arguments: List<TypeProjection> = if (transformer.transformReturnTypeGeneric) {
            originReturnType?.let {
                val variance = when {
                    it.isUnit() || it.isNothing() -> Variance.INVARIANT
                    it.isPrimitiveNumberType() -> Variance.INVARIANT
                    it.isArrayOrNullableArray() -> Variance.INVARIANT
//                    it.isEnum() -> Variance.INVARIANT
//                    it.isBoolean() -> Variance.INVARIANT
                    else -> Variance.OUT_VARIANCE
                }
                listOf(TypeProjectionImpl(variance, it))
            } ?: emptyList()
        } else {
            emptyList()
        }

        return KotlinTypeFactory.simpleType(
            TypeAttributes.Empty,
            returnTypeClass.typeConstructor,
            arguments,
            nullable = returnType.nullable
        )
    }

    open fun init() {
        val returnType = returnType(originFunction.returnType)

        initialize(
            originFunction.extensionReceiverParameter?.copy(this),
            classDescriptor.thisAsReceiverParameter,
            originFunction.contextReceiverParameters.map { it.copy(this) },
            originFunction.typeParameters.toList(),
            originFunction.valueParameters.map { it.copy(this) },
            returnType,
            modality(originFunction),
            originFunction.visibility,
            mutableMapOf<CallableDescriptor.UserDataKey<*>, Any>(SuspendTransformUserDataKey to userData)
        )
        this.isSuspend = false
    }

    protected open fun transformToPropertyInternal(): AbstractSuspendTransformProperty {
        return SimpleSuspendTransformPropertyDescriptor(
            classDescriptor,
            this,
            propertyAnnotations,
        )
    }

    fun transformToProperty(annotationData: TransformAnnotationData): AbstractSuspendTransformProperty {
        return transformToPropertyInternal().apply { init() }
    }


    protected open fun modality(originFunction: SimpleFunctionDescriptor): Modality {
        if (originFunction.modality == Modality.ABSTRACT) {
            return Modality.OPEN
        }

        return originFunction.modality
    }

}
