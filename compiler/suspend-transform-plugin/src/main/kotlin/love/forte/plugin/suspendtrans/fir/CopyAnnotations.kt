package love.forte.plugin.suspendtrans.fir

import org.jetbrains.kotlin.fir.expressions.FirAnnotation

internal data class CopyAnnotations(
    val functionAnnotations: List<FirAnnotation>,
    val propertyAnnotations: List<FirAnnotation>,
    val toOriginalAnnotations: List<FirAnnotation>
)