package love.forte.plugin.suspendtrans.fir

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.utils.CopyAnnotationsData
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.toClassId
import love.forte.plugin.suspendtrans.utils.toInfo
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.collectUpperBounds
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildProperty
import org.jetbrains.kotlin.fir.declarations.builder.buildPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunctionCopy
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
import org.jetbrains.kotlin.fir.scopes.processAllFunctions
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.argumentsCount
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
        val transformer: Transformer
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

                val (functionAnnotations, _) = copyAnnotations(originFunc, funData)

                val newFun = buildSimpleFunctionCopy(originFunc) {
                    name = callableId.callableName
                    val newFunSymbol = FirNamedFunctionSymbol(callableId)
                    symbol = newFunSymbol
                    status = originFunc.status.copy(
                        isSuspend = false,
                        modality = originFunc.syntheticModifier
                    )

                    annotations.clear()
                    annotations.addAll(functionAnnotations)
                    body = null

                    val returnType = resolveReturnType(originFunc, funData)

                    returnTypeRef = returnType

                    origin = SuspendTransformPluginKey(
                        data = SuspendTransformUserDataFir(
                            originSymbol = originFunc.symbol.asOriginSymbol(
                                typeParameters = originFunc.typeParameters,
                                valueParameters = originFunc.valueParameters,
                                originFunc.returnTypeRef.coneTypeOrNull?.classId
                            ),
                            asProperty = false,
                            transformer = funData.transformer
                        )
                    ).origin
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

                val (functionAnnotations, propertyAnnotations) =
                    copyAnnotations(original, funData)

//                val p = createMemberProperty()
//                    owner = owner,
//                    key = SuspendTransformPluginKey(
//                        data = SuspendTransformUserDataFir(
//                            originSymbol = original.symbol.asOriginSymbol(
//                                typeParameters = original.typeParameters,
//                                valueParameters = original.valueParameters,
//                                original.returnTypeRef.coneTypeOrNull?.classId
//                            ),
//                            asProperty = true,
//                            transformer = funData.transformer
//                        )
//                    ),
//                    name = callableId.callableName,
//                    returnTypeProvider = { resolveReturnConeType(original, funData) },
//                    isVal = true,
//                    hasBackingField = false,
//                ) {
//                    modality = original.syntheticModifier ?: Modality.FINAL
//                    // TODO receiver?
////                    val receiverType = original.receiverParameter?.typeRef?.coneTypeOrNull
////                    if (receiverType != null) {
////                        extensionReceiverType(receiverType)
////                    }
//                }

                val pSymbol = FirPropertySymbol(callableId)
                val pKey = SuspendTransformPluginKey(
                    data = SuspendTransformUserDataFir(
                        originSymbol = original.symbol.asOriginSymbol(
                            typeParameters = original.typeParameters,
                            valueParameters = original.valueParameters,
                            original.returnTypeRef.coneTypeOrNull?.classId
                        ),
                        asProperty = true,
                        transformer = funData.transformer
                    )
                )

                val returnType = resolveReturnType(original, funData)

                val p1 = buildProperty {
                    symbol = pSymbol
                    name = callableId.callableName
                    source = original.source
                    resolvePhase = original.resolvePhase
                    moduleData = original.moduleData
                    origin = pKey.origin
                    attributes = original.attributes.copy()
                    status = original.status.copy(
                        isSuspend = false,
                        isFun = false,
                        isInner = false,
//                        modality = if (original.status.isOverride) Modality.OPEN else original.status.modality,
                        modality = original.syntheticModifier,
                    )
                    isVar = false
                    isLocal = false
                    returnTypeRef = returnType
                    deprecationsProvider = UnresolvedDeprecationProvider //original.deprecationsProvider
                    containerSource = original.containerSource
                    dispatchReceiverType = original.dispatchReceiverType
                    contextReceivers.addAll(original.contextReceivers)
                    // annotations
                    annotations.addAll(propertyAnnotations)
                    typeParameters.addAll(original.typeParameters)
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    backingField = null
                    bodyResolveState = FirPropertyBodyResolveState.NOTHING_RESOLVED

                    getter = buildPropertyAccessor {
                        propertySymbol = pSymbol
                        symbol = FirPropertyAccessorSymbol()
                        isGetter = true
                        resolvePhase = FirResolvePhase.BODY_RESOLVE
                        moduleData = original.moduleData

                        // annotations
                        annotations.addAll(functionAnnotations)

                        returnTypeRef = returnType

                        origin = pKey.origin

//                        attributes = original.attributes.copy()
                        status = original.status.copy(
                            isSuspend = false,
                            isFun = false,
                            isInner = false,
                            modality = original.syntheticModifier,
//                            visibility = this@buildProperty.status
                        )
                        returnTypeRef = original.returnTypeRef
//                        deprecationsProvider = original.deprecationsProvider
//                        containerSource = original.containerSource
//                        dispatchReceiverType = original.dispatchReceiverType
//                        contextReceivers.addAll(original.contextReceivers)
                        valueParameters.addAll(original.valueParameters)
//                        body = null
//                        contractDescription = original.contractDescription
                        typeParameters.addAll(original.typeParameters)
                    }
                }

                propList.add(p1.symbol)
            }
        }

        return propList
    }

    private val annotationPredicates = DeclarationPredicate.create {
        var predicate: DeclarationPredicate? = null
        for (value in suspendTransformConfiguration.transformers.values) {
            for (transformer in value) {
                val afq = transformer.markAnnotation.fqName
                predicate = if (predicate == null) {
                    annotated(afq)
                } else {
                    predicate or annotated(afq)
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


                        // 读不到注解的参数？
                        // 必须使用 anno.getXxxArgument(Name(argument name)),
                        // 使用 argumentMapping.mapping 获取不到结果
//                        println("RAW AnnoData: ${anno.argumentMapping.mapping}")

                        val annoData = TransformAnnotationData.of(
                            firAnnotation = anno,
                            annotationBaseNamePropertyName = markAnnotation.baseNameProperty,
                            annotationSuffixPropertyName = markAnnotation.suffixProperty,
                            annotationAsPropertyPropertyName = markAnnotation.asPropertyProperty,
                            defaultBaseName = functionName,
                            defaultSuffix = markAnnotation.defaultSuffix,
                            defaultAsProperty = markAnnotation.defaultAsProperty,
                        )

                        map.computeIfAbsent(Name.identifier(annoData.functionName)) { ConcurrentHashMap() }[func] =
                            FunData(annoData, transformer)
                    }
                }
        }

        return map
    }

    private fun resolveReturnType(original: FirSimpleFunction, funData: FunData): FirTypeRef {
        val resultConeType = resolveReturnConeType(original, funData)

        return if (resultConeType is ConeErrorType) {
            buildErrorTypeRef {
                diagnostic = resultConeType.diagnostic
                type = resultConeType
            }
        } else {
            buildResolvedTypeRef {
                type = resultConeType
            }
        }
    }

    private fun resolveReturnConeType(original: FirSimpleFunction, funData: FunData): ConeKotlinType {
        val transformer = funData.transformer
        val returnType = transformer.transformReturnType
            ?: return original.symbol.resolvedReturnType

        var typeArguments: Array<ConeTypeProjection> = emptyArray()

        if (transformer.transformReturnTypeGeneric) {
            typeArguments = arrayOf(ConeKotlinTypeProjectionOut(original.returnTypeRef.coneType))
        }

        val resultConeType = returnType.toClassId().createConeType(
            session = session,
            typeArguments,
            nullable = returnType.nullable
        )

        return resultConeType
    }

    /**
     * @return function annotations `to` property annotations.
     */
    private fun copyAnnotations(
        original: FirSimpleFunction, funData: FunData,
    ): Pair<List<FirAnnotation>, List<FirAnnotation>> {
        val transformer = funData.transformer

        val (copyFunction, excludes, includes) = CopyAnnotationsData(
            transformer.copyAnnotationsToSyntheticFunction,
            transformer.copyAnnotationExcludes.map { it.toClassId() },
            transformer.syntheticFunctionIncludeAnnotations.map { it.toInfo() }
        )

        val annotationList = mutableListOf<FirAnnotation>()

        with(annotationList) {
            if (copyFunction) {
                val notCompileAnnotationsCopied = original.annotations.filterNot {
                    val annotationClassId = it.toAnnotationClassId(session) ?: return@filterNot true
                    excludes.any { ex -> annotationClassId == ex }
                }

                addAll(notCompileAnnotationsCopied)
            }

            // try add @Generated(by = ...)
//            runCatching {
//                val generatedAnnotation = buildAnnotation {
//                    annotationTypeRef = buildResolvedTypeRef {
//                        type = generatedAnnotationClassId.createConeType(session)
//                    }
//                    argumentMapping = buildAnnotationArgumentMapping {
//                        includeGeneratedArguments(original)
//                    }
//                }
//                add(generatedAnnotation)
//            }.getOrElse { e ->
//                // Where is log?
//                e.printStackTrace()
//            }

            // add includes
            includes.forEach { include ->
                val classId = include.classId
                val includeAnnotation = buildAnnotation {
                    argumentMapping = buildAnnotationArgumentMapping()
                    annotationTypeRef = buildResolvedTypeRef {
                        type = classId.createConeType(session)
                    }
                }
                add(includeAnnotation)
            }
        }

        return annotationList to emptyList()
    }


    private fun FirAnnotationArgumentMappingBuilder.includeGeneratedArguments(function: FirSimpleFunction) {
        fun MutableList<FirExpression>.addString(value: String) {
            val expression = buildConstExpression(
                source = null,
                kind = ConstantValueKind.String,
                value = value,
                setType = false
            )
            add(expression)
        }

        fun ConeKotlinType.typeString(): String {
            return buildString {
                append(classId?.asFqNameString())
                collectUpperBounds()
                    .takeIf { it.isNotEmpty() }
                    ?.also { upperBounds ->
                        upperBounds.joinTo(this, "&") { type ->
                            type.classId?.asFqNameString() ?: "?NULL?"
                        }
                    }
                if (kotlin.runCatching { argumentsCount() }.getOrElse { 0 } > 0) {
                    typeArguments.joinTo(this, ", ", "<", ">") { argument ->
                        argument.type?.classId?.asFqNameString() ?: "?NULL?"
                    }
                }
            }
        }

        with(mapping) {
            put(Name.identifier("by"), buildArrayLiteral {
                argumentList = buildArgumentList {
                    with(arguments) {
                        addString(function.name.asString())
                        function.valueParameters.forEach { vp ->
                            addString(vp.name.asString())
                            vp.returnTypeRef.coneTypeOrNull?.also { coneType ->
                                addString(coneType.typeString())
                            }
                        }
                        addString(function.returnTypeRef.coneTypeOrNull?.typeString() ?: "?")
                    }
                }
            })
        }
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

private val FirSimpleFunction.syntheticModifier: Modality?
    get() = when {
        status.isOverride -> Modality.OPEN
        modality == Modality.ABSTRACT -> Modality.OPEN
        else -> status.modality
    }
