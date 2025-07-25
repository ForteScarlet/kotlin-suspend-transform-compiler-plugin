package love.forte.plugin.suspendtrans.fir

import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol

internal data class FirCacheKey(
    val classSymbol: FirClassSymbol<*>,
    val memberScope: FirClassDeclaredMemberScope?
)