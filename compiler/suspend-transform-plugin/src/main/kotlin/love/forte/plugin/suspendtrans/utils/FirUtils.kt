package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.FirAnnotation

fun FirSimpleFunction.includeAnnotations(includes: List<FirAnnotation>) {
    replaceAnnotations(symbol.resolvedAnnotationsWithArguments + includes)
}
