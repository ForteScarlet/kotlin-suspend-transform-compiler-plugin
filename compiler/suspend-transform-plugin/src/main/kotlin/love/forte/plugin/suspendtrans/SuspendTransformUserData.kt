package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

object SuspendTransformUserDataKey : CallableDescriptor.UserDataKey<SuspendTransformUserData>

data class SuspendTransformUserData(
    val originFunction: SimpleFunctionDescriptor,
    val asProperty: Boolean,
    val transformer: Transformer
) {
    val originFunctionSymbol = originFunction.toOriginFunctionSymbol()
}

fun SimpleFunctionDescriptor.toOriginFunctionSymbol(): OriginFunctionSymbol {
    val fqName = fqNameSafe

    val typeParameters = typeParameters.map {
        OriginTypeParameter(
            name = it.name,
            varianceOrdinal = it.variance.ordinal,
            isReified = it.isReified,
            it.upperBounds.map { bound ->
                kotlin.runCatching { bound.constructor.declarationDescriptor?.name }.getOrNull()
            }
        )
    }

    val valueParameters = valueParameters.map {
        OriginValueParameter(
            name = it.name,
            typeFqName = kotlin.runCatching { it.type.getKotlinTypeFqName(false) }.getOrNull()
        )
    }


    val returnType = kotlin.runCatching { returnType?.getKotlinTypeFqName(false) }.getOrNull()

    return OriginFunctionSymbol(
        name = fqName,
        typeParameters = typeParameters,
        valueParameters = valueParameters,
        returnType = returnType
    )
}

fun OriginFunctionSymbol.isSame(irFunction: IrFunction): Boolean {
    // function name
    if (!irFunction.hasEqualFqName(name)) return false

    // return type
    if (irFunction.returnType.classFqName?.asString() != returnType) return false

    // typeParameters
    val irFunctionTypeParameters = irFunction.typeParameters
    if (irFunction.typeParameters.size != typeParameters.size) return false

    for ((index, typeParameter) in irFunctionTypeParameters.withIndex()) {
        val targetTypeParameter = typeParameters[index]
        if (!(typeParameter isSameAs targetTypeParameter)) return false
    }


    // valueParameters
    val irFunctionValueParameters = irFunction.valueParameters
    if (irFunction.valueParameters.size != valueParameters.size) return false

    for ((index, valueParameter) in irFunctionValueParameters.withIndex()) {
        val targetValueParameter = valueParameters[index]
        if (!(valueParameter isSameAs targetValueParameter)) return false
    }

    return true
}

private infix fun IrTypeParameter.isSameAs(typeParameter: OriginTypeParameter): Boolean {
    if (name != typeParameter.name) return false
    if (variance.ordinal != typeParameter.varianceOrdinal) return false
    if (isReified != typeParameter.isReified) return false
    val superTypes = superTypes
    if (superTypes.size != typeParameter.upperBoundNames.size) return false

    for ((index, superType) in superTypes.withIndex()) {
        val typeBoundName = typeParameter.upperBoundNames[index]
        if (superType.classFqName?.shortName() != typeBoundName) return false
    }

    return true
}

private infix fun IrValueParameter.isSameAs(valueParameter: OriginValueParameter): Boolean {
    if (name != valueParameter.name) return false
    return type.classFqName?.asString() == valueParameter.typeFqName
}

data class OriginFunctionSymbol(
    val name: FqName,
    val typeParameters: List<OriginTypeParameter>,
    val valueParameters: List<OriginValueParameter>,
    val returnType: String?,
)

data class OriginTypeParameter(
    val name: Name,
    val varianceOrdinal: Int,
    val isReified: Boolean,
    val upperBoundNames: List<Name?>
)

data class OriginValueParameter(
    val name: Name,
    val typeFqName: String?
)

////

data class SuspendTransformUserDataFir(
    val originSymbol: OriginSymbol,
    val asProperty: Boolean,
    val transformer: Transformer
)

fun FirNamedFunctionSymbol.asOriginSymbol(
    typeParameters: List<FirTypeParameter>,
    valueParameters: List<FirValueParameter>,
    returnType: ClassId?
): OriginSymbol {
    return OriginSymbol(
        callableId = this.callableId,
        typeParameters = typeParameters.map { it.toTypeParameter() },
        valueParameters = valueParameters.map { it.toValueParameter() },
        returnType
    )
}

data class OriginSymbol(
    val callableId: CallableId,
    val typeParameters: List<TypeParameter>,
    val valueParameters: List<ValueParameter>,
    val returnType: ClassId?
)

data class TypeParameter(val name: Name, val varianceOrdinal: Int, val isReified: Boolean, val bounds: List<ClassId?>)

private fun FirTypeParameter.toTypeParameter(): TypeParameter =
    TypeParameter(
        name,
        variance.ordinal,
        isReified,
        bounds.map { it.coneTypeOrNull?.classId }
    )


data class ValueParameter(val name: Name, val type: ClassId?)

private fun FirValueParameter.toValueParameter(): ValueParameter =
    ValueParameter(name, returnTypeRef.coneTypeOrNull?.type?.classId)


fun OriginSymbol.checkSame(declaration: IrFunction): Boolean {
    // callableId
    if (callableId != declaration.callableId) return false
    // return type
    if (declaration.returnType.classFqName != returnType?.asSingleFqName()) return false
    // typeParameters
    val declarationTypeParameters = declaration.typeParameters
    if (typeParameters.size != declarationTypeParameters.size) return false
    for ((index, typeParameter) in declarationTypeParameters.withIndex()) {
        val targetTypeParameter = typeParameters[index]
        if (!(typeParameter isSameAs targetTypeParameter)) return false
    }

    // valueParameters
    val declarationValueParameters = declaration.valueParameters
    if (valueParameters.size != declarationValueParameters.size) return false
    for ((index, valueParameter) in declarationValueParameters.withIndex()) {
        val targetValueParameter = valueParameters[index]
        if (!(valueParameter isSameAs targetValueParameter)) return false
    }

    return true
}

private infix fun IrTypeParameter.isSameAs(typeParameter: TypeParameter): Boolean {
    if (name != typeParameter.name) return false
    if (variance.ordinal != typeParameter.varianceOrdinal) return false
    if (isReified != typeParameter.isReified) return false
    val superTypes = superTypes
    if (superTypes.size != typeParameter.bounds.size) return false

    for ((index, superType) in superTypes.withIndex()) {
        val typeBound = typeParameter.bounds[index]
        if (superType.classFqName != typeBound?.asSingleFqName()) return false
    }

    return true
}

private infix fun IrValueParameter.isSameAs(valueParameter: ValueParameter): Boolean {
    if (name != valueParameter.name) return false
    return type.classFqName == valueParameter.type?.asSingleFqName()
}
