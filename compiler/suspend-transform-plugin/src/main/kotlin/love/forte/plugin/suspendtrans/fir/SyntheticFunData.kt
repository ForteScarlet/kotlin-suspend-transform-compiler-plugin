package love.forte.plugin.suspendtrans.fir

import love.forte.plugin.suspendtrans.configuration.Transformer
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform

internal data class SyntheticFunData(
    val funName: Name,
    val annotationData: TransformAnnotationData,
    val transformer: Transformer,
    val transformerFunctionSymbol: FirNamedFunctionSymbol?,
    val markAnnotationTypeArgument: FirTypeProjection?,
    val platform: TargetPlatform
) {
    fun transformerFunctionSymbol(
        transformerFunctionSymbolMap: Map<Transformer, FirNamedFunctionSymbol>,
        finder: (Transformer) -> FirNamedFunctionSymbol?
    ): FirNamedFunctionSymbol {
        return transformerFunctionSymbol
            ?: transformerFunctionSymbolMap[transformer]
            ?: finder(transformer)
            ?: error("Cannot find transformer function symbol for transformer: $transformer in $platform")
    }

}