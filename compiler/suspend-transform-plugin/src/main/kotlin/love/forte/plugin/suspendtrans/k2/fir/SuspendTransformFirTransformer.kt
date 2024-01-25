package love.forte.plugin.suspendtrans.k2.fir

import love.forte.plugin.suspendtrans.MarkAnnotation
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.TargetPlatform
import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.builder.buildProperty
import org.jetbrains.kotlin.fir.declarations.builder.buildPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunctionCopy
import org.jetbrains.kotlin.fir.declarations.getAnnotationsByClassId
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.declarations.resolvePhase
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
import org.jetbrains.kotlin.fir.scopes.processAllFunctions
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformFirTransformer(
    session: FirSession,
    private val suspendTransformConfiguration: SuspendTransformConfiguration
) : FirDeclarationGenerationExtension(session) {

    //    private val cache: FirCache<Pair<FirClassSymbol<*>, FirClassDeclaredMemberScope?>, Map<Name, FirJavaMethod>?, Nothing?> =
//        session.firCachesFactory.createCache(uncurry(::createGetters))
//        session.firCachesFactory.createCache { (symbol, declaredScope), context ->
//            createTransformers(symbol, declaredScope)
//        }

    private data class FunData(
        val annotationData: TransformAnnotationData,
    )

    private val cache: FirCache<Pair<FirClassSymbol<*>, FirClassDeclaredMemberScope?>, Map<Name, Map<FirNamedFunctionSymbol, FunData>>?, Nothing?> =
        session.firCachesFactory.createCache { (symbol, scope), c ->
            createCache(symbol, scope)
        }


    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val names = mutableSetOf<Name>()

        cache.getValue(classSymbol to context.declaredScope)?.forEach { (_, map) ->
            map.values.forEach { names.add(Name.identifier(it.annotationData.functionName)) }
        }

        return names
    }

    @OptIn(SymbolInternals::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        val funcMap = cache.getValue(owner to context.declaredScope)?.get(callableId.callableName) ?: return emptyList()

        val funList = mutableListOf<FirNamedFunctionSymbol>()

        funcMap.forEach { (func, funData) ->
            val annotationData = funData.annotationData
            if (!annotationData.asProperty) {
                // generate

                val originFunc = func.fir

                val newFun = buildSimpleFunctionCopy(originFunc) {
                    name = callableId.callableName
                    symbol = FirNamedFunctionSymbol(callableId)
                    status = originFunc.status.copy(
                        isSuspend = false,
                        modality = if (originFunc.status.isOverride) Modality.OPEN else originFunc.status.modality,
                    )
                    origin = SuspendTransformPluginKey.origin
                }

                funList.add(newFun.symbol)
            }
        }


        return funList
    }

    @OptIn(SymbolInternals::class)
    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()
        val funcMap = cache.getValue(owner to context.declaredScope)?.get(callableId.callableName) ?: return emptyList()

        val propList = mutableListOf<FirPropertySymbol>()

        funcMap.forEach { (func, funData) ->
            val annotationData = funData.annotationData
            if (annotationData.asProperty) {
                // generate
                val original = func.fir

                val p = buildProperty {
                    name = callableId.callableName
                    symbol = FirPropertySymbol(callableId)
                    source = original.source
                    resolvePhase = original.resolvePhase
                    moduleData = original.moduleData
                    origin = original.origin
                    attributes = original.attributes.copy()
                    status = original.status.copy(
                        isSuspend = false,
                        modality = if (original.status.isOverride) Modality.OPEN else original.status.modality,
                    )
                    returnTypeRef = original.returnTypeRef
                    deprecationsProvider = original.deprecationsProvider
                    containerSource = original.containerSource
                    dispatchReceiverType = original.dispatchReceiverType
                    contextReceivers.addAll(original.contextReceivers)
                    annotations.addAll(original.annotations) // TODO
                    typeParameters.addAll(original.typeParameters)
                    getter = buildPropertyAccessor {
                        symbol = FirPropertyAccessorSymbol()
                        source = original.source
                        resolvePhase = original.resolvePhase
                        moduleData = original.moduleData
                        origin = original.origin
                        attributes = original.attributes.copy()
                        status = original.status
                        returnTypeRef = original.returnTypeRef
                        deprecationsProvider = original.deprecationsProvider
                        containerSource = original.containerSource
                        dispatchReceiverType = original.dispatchReceiverType
                        contextReceivers.addAll(original.contextReceivers)
                        valueParameters.addAll(original.valueParameters)
                        body = original.body
                        contractDescription = original.contractDescription
                        annotations.addAll(original.annotations) // TODO
                        typeParameters.addAll(original.typeParameters)
                    }
                }

                propList.add(p.symbol)
            }
        }

        return propList
    }

    private val annotationPredicates = DeclarationPredicate.create {
        var predicate: DeclarationPredicate? = null
        for (value in suspendTransformConfiguration.transformers.values) {
            for (transformer in value) {
                val afq = transformer.markAnnotation.fqName
                if (predicate == null) {
                    predicate = metaAnnotated(setOf(afq), false)
                } else {
                    predicate = predicate or metaAnnotated(setOf(afq), false)
                }
            }
        }

        predicate ?: annotated()
    }

    /**
     * NB: The predict needs to be *registered* in order to parse the [@XSerializable] type
     * otherwise, the annotation remains unresolved
     */
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {


//        val serializable = suspendTransformConfiguration.transformers.values
//            .asSequence()
//            .flatMap {
//                it.map { t ->
//                    DeclarationPredicate.create { annotated(t.markAnnotation.fqName) }
//                }
//            }
//            .reduce { a, b -> a.or(b) }

//        val serializable =
//            LookupPredicate.create {
//                val annotationFqNames = suspendTransformConfiguration.transformers.values.flatMapTo(mutableSetOf()) {
//                    it.map { t ->
//                        t.markAnnotation.fqName
//                    }
//                }
//
//                annotated(annotationFqNames)
//            }

        register(annotationPredicates)
    }

    private fun createCache(
        classSymbol: FirClassSymbol<*>,
        declaredScope: FirClassDeclaredMemberScope?
    ): Map<Name, Map<FirNamedFunctionSymbol, FunData>>? {
        if (declaredScope == null) return null


        fun check(targetPlatform: TargetPlatform): Boolean {
            val platform = classSymbol.moduleData.platform

            return when {
                platform.isJvm() && targetPlatform == TargetPlatform.JVM -> true
                platform.isJs() && targetPlatform == TargetPlatform.JS -> true
                platform.isWasm() && targetPlatform == TargetPlatform.WASM -> true
                platform.isNative() && targetPlatform == TargetPlatform.NATIVE -> true
                platform.isCommon() && targetPlatform == TargetPlatform.COMMON -> true
                else -> false
            }
        }

        val map = ConcurrentHashMap<Name, MutableMap<FirNamedFunctionSymbol, FunData>>()

        declaredScope.processAllFunctions { func ->
            if (!func.isSuspend) return@processAllFunctions
//            if (!func.visibility.isPublicAPI) return@processAllFunctions

            val functionName = func.name.asString()
            suspendTransformConfiguration.transformers.asSequence()
                .filter { (platform, _) -> check(platform) }
                .forEach { (_, transformerList) ->
                    for (transformer in transformerList) {
                        val markAnnotation = transformer.markAnnotation

                        val anno = func.resolvedAnnotationsWithArguments.getAnnotationsByClassId(
                            markAnnotation.classId,
                            session
                        ).firstOrNull()
                            ?: classSymbol.resolvedAnnotationsWithArguments.getAnnotationsByClassId(
                                markAnnotation.classId,
                                session
                            ).firstOrNull()
                            ?: continue

                        println("!!${functionName}.anno: ${anno.fqName(session)}")
                        println("!!${functionName}.anno.mapping: ${anno.argumentMapping.mapping}")
                        println()

                        // TODO 读不到注解的参数？

                        val annoData = TransformAnnotationData.of(
                            firAnnotation = anno,
                            annotationBaseNamePropertyName = markAnnotation.baseNameProperty,
                            annotationSuffixPropertyName = markAnnotation.suffixProperty,
                            annotationAsPropertyPropertyName = markAnnotation.asPropertyProperty,
                            defaultBaseName = functionName,
                            defaultSuffix = markAnnotation.defaultSuffix,
                            defaultAsProperty = markAnnotation.defaultAsProperty,
                        )

                        println("AnnoData: $annoData")

                        map.computeIfAbsent(Name.identifier(annoData.functionName)) { ConcurrentHashMap() }[func] =
                            FunData(annoData)
                    }
                }
        }

        return map
    }
}


private val MarkAnnotation.classId: ClassId
    get() {
        return ClassId(classInfo.packageName.fqn, classInfo.className.fqn, classInfo.local)
    }


private val MarkAnnotation.fqName: FqName
    get() {
        return FqName(classInfo.packageName + "." + classInfo.className)
    }
