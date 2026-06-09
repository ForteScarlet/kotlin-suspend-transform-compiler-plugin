/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package love.forte.plugin.suspendtrans.fir

import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.configuration.Transformer
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.MutableCheckerContext
import org.jetbrains.kotlin.fir.caches.*
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.SessionHolderImpl
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculatorForFullBodyResolve
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import java.util.concurrent.ConcurrentHashMap

/**
 * FIR declaration generator responsible for exposing non-suspend synthetic members
 * for configured suspend declarations.
 *
 * @author ForteScarlet
 */
class SuspendTransformFirTransformer(
    session: FirSession,
    internal val suspendTransformConfiguration: SuspendTransformConfiguration
) : FirDeclarationGenerationExtension(session) {
    /** FIR session used for symbol resolution and generated type reconstruction. */
    internal val firSession: FirSession = session

    /** Cached bridge-function symbols keyed by transformer configuration. */
    internal val transformerFunctionSymbolMap =
        ConcurrentHashMap<Transformer, FirNamedFunctionSymbol>()

    /** Lazily resolved `CoroutineScope` symbol used by generated bridge calls. */
    internal lateinit var coroutineScopeSymbol: FirClassLikeSymbol<*>

    internal fun isCoroutineScopeSymbolInitialized(): Boolean = ::coroutineScopeSymbol.isInitialized

    /**
     * Per-class synthetic member cache grouped by generated callable name.
     *
     * The cache value maps generated names to the source suspend declarations that
     * should produce that synthetic member.
     */
    internal val cache: FirCache<FirCacheKey, Map<Name, Map<FirNamedFunctionSymbol, SyntheticFunData>>?, Nothing?> =
        firSession.firCachesFactory.createCache { cacheKey, _ ->
            val (symbol, scope) = cacheKey
            initScopeSymbol()
            val transformerFunctionMap = initTransformerFunctionSymbolMap(symbol, scope)
            createCache(symbol, scope, transformerFunctionMap)
        }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val names = mutableSetOf<Name>()

        cache.getValue(FirCacheKey(classSymbol, context.declaredScope))?.forEach { (_, map) ->
            map.values.forEach { names.add(it.funName) }
        }

        return names
    }

    /** Scope session reused while evaluating override relationships for generated members. */
    internal val scope = ScopeSession()
    internal val holder = SessionHolderImpl(firSession, scope)

    /** Checker context shared by FIR override checks for synthetic declarations. */
    internal val checkContext = MutableCheckerContext(
        holder,
        ReturnTypeCalculatorForFullBodyResolve.Default,
    )

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        val funcMap = cache.getValue(FirCacheKey(owner, context.declaredScope))
            ?.get(callableId.callableName)
            ?: return emptyList()

        val funList = arrayListOf<FirNamedFunctionSymbol>()

        funcMap.forEach { (func, funData) ->
            generateSyntheticFunctions(
                callableId,
                owner,
                func,
                funData,
                funList,
            )
        }

        return funList
    }

    @OptIn(SymbolInternals::class)
    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> = generateSyntheticProperties(callableId, context)

    /** Predicate matching every configured marker annotation used by the plugin. */
    internal val annotationPredicates = DeclarationPredicate.create {
        val annotationFqNames = suspendTransformConfiguration.transformers.values
            .flatMapTo(mutableSetOf()) { transformerList ->
                transformerList.map { it.markAnnotation.fqName }
            }

        hasAnnotated(annotationFqNames)
        // var predicate: DeclarationPredicate? = null
        // for (value in suspendTransformConfiguration.transformers.values) {
        //     for (transformer in value) {
        //         val afq = transformer.markAnnotation.fqName
        //         predicate = if (predicate == null) {
        //             annotated(afq)
        //         } else {
        //             predicate or annotated(afq)
        //         }
        //     }
        // }
        //
        // predicate ?: annotated()
    }

    /**
     * NB: The predict needs to be *registered* in order to parse the [@XSerializable] type
     * otherwise, the annotation remains unresolved
     */
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(annotationPredicates)
    }
}
