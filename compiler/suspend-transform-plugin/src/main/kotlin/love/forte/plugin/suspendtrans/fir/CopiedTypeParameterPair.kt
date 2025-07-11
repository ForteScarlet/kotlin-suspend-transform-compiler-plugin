package love.forte.plugin.suspendtrans.fir

import org.jetbrains.kotlin.fir.declarations.FirTypeParameter

internal data class CopiedTypeParameterPair(
    val original: FirTypeParameter,
    val copied: FirTypeParameter
)