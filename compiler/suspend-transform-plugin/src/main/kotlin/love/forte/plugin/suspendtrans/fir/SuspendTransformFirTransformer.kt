package love.forte.plugin.suspendtrans.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.java.declarations.FirJavaMethod
import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformFirTransformer(private val _session: FirSession) :
    FirDeclarationGenerationExtension(_session) {


    private val cache : FirCache<Pair<FirClassSymbol<*>, FirClassDeclaredMemberScope?>, Map<Name, FirJavaMethod>?, Nothing?> =
//        session.firCachesFactory.createCache(uncurry(::createGetters))
        session.firCachesFactory.createCache { (symbol, declaredScope), context ->
            createGetters(symbol, declaredScope)
            TODO()
        }


    override fun getTopLevelCallableIds(): Set<CallableId> {
        return super.getTopLevelCallableIds()
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        return super.getCallableNamesForClass(classSymbol, context)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        return super.generateFunctions(callableId, context)
    }


    private fun createGetters(classSymbol: FirClassSymbol<*>, declaredScope: FirClassDeclaredMemberScope?): Map<Name, FirJavaMethod>? {

        TODO()
    }
}

internal inline fun <A, B, C> uncurry(crossinline f: (A, B) -> C): (Pair<A, B>) -> C = { (a, b) -> f(a, b) }
