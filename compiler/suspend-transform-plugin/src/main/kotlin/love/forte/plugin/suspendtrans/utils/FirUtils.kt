package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.FirResolvedTypeRefBuilder
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef

internal fun FirSimpleFunction.includeAnnotations(includes: List<FirAnnotation>) {
    replaceAnnotations(symbol.resolvedAnnotationsWithArguments + includes)
}

internal inline fun ConeKotlinType.toResolvedTypeRef(
    block: FirResolvedTypeRefBuilder.() -> Unit = {}
): FirResolvedTypeRef = buildResolvedTypeRef {
    coneType = this@toResolvedTypeRef
    block()
}