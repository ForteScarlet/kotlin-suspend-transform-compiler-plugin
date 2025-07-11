package love.forte.plugin.suspendtrans.fir

import love.forte.plugin.suspendtrans.annotation.ExperimentalReturnTypeOverrideGenericApi
import love.forte.plugin.suspendtrans.configuration.*
import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.utils.*
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.context.MutableCheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.processOverriddenFunctionsSafe
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.references.builder.buildExplicitThisReference
import org.jetbrains.kotlin.fir.references.builder.buildImplicitThisReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.SessionHolderImpl
import org.jetbrains.kotlin.fir.resolve.getSuperTypes
import org.jetbrains.kotlin.fir.resolve.providers.dependenciesSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculatorForFullBodyResolve
import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.scopes.processAllFunctions
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.utils.keysToMap
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformFirTransformer(
    session: FirSession,
    private val suspendTransformConfiguration: SuspendTransformConfiguration
) : FirDeclarationGenerationExtension(session) {

    private val transformerFunctionSymbolMap =
        ConcurrentHashMap<Transformer, FirNamedFunctionSymbol>()

    private val allMarkAnnotationClassInfos: Set<ClassInfo> =
        suspendTransformConfiguration.transformers.values.flatMapTo(mutableSetOf()) { transformerList ->
            transformerList.map { transformer -> transformer.markAnnotation.classInfo }
        }

    private lateinit var coroutineScopeSymbol: FirClassLikeSymbol<*>

    private fun initScopeSymbol() {
        val classId = ClassId(
            FqName.fromSegments(listOf("kotlinx", "coroutines")),
            Name.identifier("CoroutineScope")
        )

        if (!(::coroutineScopeSymbol.isInitialized)) {
            coroutineScopeSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)
                ?: session.dependenciesSymbolProvider.getClassLikeSymbolByClassId(classId)
                        ?: error("Cannot resolve `kotlinx.coroutines.CoroutineScope` symbol.")
        }
    }

    private fun findTransformerFunctionSymbol(transformer: Transformer): FirNamedFunctionSymbol? {
        val symbolProvider = session.symbolProvider
        val dependenciesSymbolProvider = session.dependenciesSymbolProvider

        val transformFunctionInfo = transformer.transformFunctionInfo
        val packageName = transformFunctionInfo.packageName
        val functionName = transformFunctionInfo.functionName

        val functionNameIdentifier = Name.identifier(functionName)

        val transformerFunctionSymbols =
            symbolProvider.getTopLevelFunctionSymbols(
                packageName.fqn,
                functionNameIdentifier
            ).ifEmpty {
                dependenciesSymbolProvider.getTopLevelFunctionSymbols(
                    packageName.fqn,
                    functionNameIdentifier
                )
            }

        if (transformerFunctionSymbols.isNotEmpty()) {
            if (transformerFunctionSymbols.size == 1) {
                val firstFunctionSymbol = transformerFunctionSymbols.first()
                transformerFunctionSymbolMap[transformer] = firstFunctionSymbol
                return firstFunctionSymbol
            } else {
                error("Found multiple transformer function symbols for transformer: $transformer")
            }
        } else {
            // 有时候在不同平台中寻找，可能会找不到，例如在jvm中找不到js的函数
            // error("Cannot find transformer function symbol $packageName.$functionName (${session.moduleData.platform}) for transformer: $transformer")
        }

        return null
    }

    private fun initTransformerFunctionSymbolMap(): Map<Transformer, FirNamedFunctionSymbol> {
        // 尝试找到所有配置的 bridge function, 例如 `runBlocking` 等
        val map = mutableMapOf<Transformer, FirNamedFunctionSymbol>()

        suspendTransformConfiguration.transformers
            .forEach { (_, transformerList) ->
                for (transformer in transformerList) {
                    val transformerFunctionSymbol = findTransformerFunctionSymbol(transformer)
                    if (transformerFunctionSymbol != null) {
                        map[transformer] = transformerFunctionSymbol
                    }
                }
            }

        return map
    }

    private val cache: FirCache<FirCacheKey, Map<Name, Map<FirNamedFunctionSymbol, SyntheticFunData>>?, Nothing?> =
        session.firCachesFactory.createCache { cacheKey, c ->
            val (symbol, scope) = cacheKey
            initScopeSymbol()
            val transformerFunctionMap = initTransformerFunctionSymbolMap()
            createCache(symbol, scope, transformerFunctionMap)
        }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val names = mutableSetOf<Name>()

        cache.getValue(FirCacheKey(classSymbol, context.declaredScope))?.forEach { (_, map) ->
            map.values.forEach { names.add(it.funName) }
        }

        return names
    }

    private val scope = ScopeSession()
    private val holder = SessionHolderImpl(session, scope)
    private val checkContext = MutableCheckerContext(
        holder,
        ReturnTypeCalculatorForFullBodyResolve.Default,
    )

    private fun List<FirTypeParameter>.mapToNewTypeParameters(
        funSymbol: FirBasedSymbol<*>,
        originalTypeParameterCache: MutableList<CopiedTypeParameterPair>
    ): List<FirTypeParameter> {
        return map {
            buildTypeParameterCopy(it) {
                containingDeclarationSymbol = funSymbol // it.containingDeclarationSymbol
//                             symbol = it.symbol // FirTypeParameterSymbol()
                symbol = FirTypeParameterSymbol()
            }.also { new ->
                originalTypeParameterCache.add(CopiedTypeParameterPair(it, new))
            }
        }
    }

    private fun List<FirValueParameter>.mapToNewValueParameters(
        originalTypeParameterCache: MutableList<CopiedTypeParameterPair>,
        newContainingDeclarationSymbol: FirBasedSymbol<*>
    ): List<FirValueParameter> {
        return map { vp ->
            buildValueParameterCopy(vp) {
                symbol = FirValueParameterSymbol(vp.symbol.name)
                containingDeclarationSymbol = newContainingDeclarationSymbol

                val copiedConeType = vp.returnTypeRef.coneTypeOrNull
                    ?.copyWithTypeParameters(originalTypeParameterCache)

                if (copiedConeType != null) {
                    returnTypeRef = returnTypeRef.withReplacedConeType(copiedConeType)
                }
            }
        }
    }

    private fun FirReceiverParameter.copyToNew(
        originalTypeParameterCache: MutableList<CopiedTypeParameterPair>,
        newContainingDeclarationSymbol: FirBasedSymbol<*>
    ): FirReceiverParameter? {
        return typeRef.coneTypeOrNull
            ?.copyWithTypeParameters(originalTypeParameterCache)
            ?.let { foundCopied ->
                buildReceiverParameterCopy(this) {
                    symbol = FirReceiverParameterSymbol()
                    containingDeclarationSymbol = newContainingDeclarationSymbol
                    typeRef = typeRef.withReplacedConeType(foundCopied)
                }
            }
    }


    private fun ConeKotlinType.copyConeType(originalTypeParameterCache: MutableList<CopiedTypeParameterPair>): ConeKotlinType? {
        return copyWithTypeParameters(originalTypeParameterCache)
    }

    private fun ConeKotlinType.copyConeTypeOrSelf(originalTypeParameterCache: MutableList<CopiedTypeParameterPair>): ConeKotlinType {
        return copyConeType(originalTypeParameterCache) ?: this
    }

    private fun FirSimpleFunctionBuilder.copyParameters(originalTypeParameterCache: MutableList<CopiedTypeParameterPair>) {
        val newFunSymbol = symbol

        val newTypeParameters = typeParameters.mapToNewTypeParameters(newFunSymbol, originalTypeParameterCache)
        typeParameters.clear()
        typeParameters.addAll(newTypeParameters)

        val newContextParameters = contextParameters.mapToNewValueParameters(
            originalTypeParameterCache,
            newFunSymbol,
        )
        contextParameters.clear()
        contextParameters.addAll(newContextParameters)

        val newValueParameters = valueParameters.mapToNewValueParameters(
            originalTypeParameterCache,
            newFunSymbol
        )
        valueParameters.clear()
        valueParameters.addAll(newValueParameters)

        receiverParameter?.copyToNew(originalTypeParameterCache, newFunSymbol)?.also {
            this.receiverParameter = it
        }

        val coneTypeOrNull = returnTypeRef.coneTypeOrNull
        if (coneTypeOrNull != null) {
            returnTypeRef = returnTypeRef
                .withReplacedConeType(coneTypeOrNull.copyConeTypeOrSelf(originalTypeParameterCache))
        }
    }

    private fun FirPropertyAccessorBuilder.copyParameters(
        originalTypeParameterCache: MutableList<CopiedTypeParameterPair> = mutableListOf(),
        copyReturnType: Boolean = true,
        newFunSymbol: FirBasedSymbol<*>,
    ) {
        // 的确，property 哪儿来的 type parameter
//        val newTypeParameters = typeParameters.mapToNewTypeParameters(symbol, originalTypeParameterCache)
//        typeParameters.clear()
//        typeParameters.addAll(newTypeParameters)

        val newValueParameters = valueParameters.mapToNewValueParameters(
            originalTypeParameterCache,
            newFunSymbol
        )
        valueParameters.clear()
        valueParameters.addAll(newValueParameters)

        if (copyReturnType) {
            val coneTypeOrNull = returnTypeRef.coneTypeOrNull
            if (coneTypeOrNull != null) {
                returnTypeRef = returnTypeRef
                    .withReplacedConeType(coneTypeOrNull.copyConeTypeOrSelf(originalTypeParameterCache))
            }
        }
    }

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
    private fun generateSyntheticFunctionBody(
        originFunc: FirSimpleFunction,
        originFunSymbol: FirNamedFunctionSymbol,
        owner: FirClassSymbol<*>,
        thisContextParameters: List<FirValueParameter>,
        thisReceiverParameter: FirReceiverParameter?,
        newFunSymbol: FirBasedSymbol<*>,
        thisValueParameters: List<FirValueParameter>,
        bridgeFunSymbol: FirNamedFunctionSymbol,
        newFunTarget: FirFunctionTarget,
        funData: SyntheticFunData,
        originalTypeParameterCache: MutableList<CopiedTypeParameterPair>,
        returnTypeRef: FirTypeRef
    ): FirBlock = buildBlock {
        this.source = originFunc.body?.source

        // lambda: suspend () -> T
        val lambdaTarget = FirFunctionTarget(null, isLambda = true)
        val lambda = buildAnonymousFunction {
            this.resolvePhase = FirResolvePhase.BODY_RESOLVE
            // this.resolvePhase = FirResolvePhase.RAW_FIR
            this.isLambda = true
            this.moduleData = originFunSymbol.moduleData
            // this.origin = FirDeclarationOrigin.Source
            // this.origin = FirDeclarationOrigin.Synthetic.FakeFunction
            this.origin = FirDeclarationOrigin.Plugin(SuspendTransformK2V3Key)
            this.returnTypeRef = originFunSymbol.resolvedReturnTypeRef
            this.hasExplicitParameterList = false
            // this.status = FirResolvedDeclarationStatusImpl.DEFAULT_STATUS_FOR_SUSPEND_FUNCTION_EXPRESSION
            this.status = this.status.copy(isSuspend = true)
            this.symbol = FirAnonymousFunctionSymbol()
            this.body = buildSingleExpressionBlock(
                buildReturnExpression {
                    target = lambdaTarget
                    result = buildFunctionCall {
                        // Call original fun
                        this.coneTypeOrNull = originFunSymbol.resolvedReturnTypeRef.coneType
                        this.source = originFunSymbol.source
                        this.calleeReference = buildResolvedNamedReference {
                            this.source = originFunSymbol.source
                            this.name = originFunSymbol.name
                            this.resolvedSymbol = originFunSymbol
                        }

                        val originValueParameters = originFunc.valueParameters

                        this.dispatchReceiver = buildThisReceiverExpression {
                            coneTypeOrNull = originFunSymbol.dispatchReceiverType
                            source = originFunSymbol.source
                            calleeReference = buildImplicitThisReference {
                                boundSymbol = owner
                            }
                        }

                        this.contextArguments.addAll(thisContextParameters.map { receiver ->
                            buildThisReceiverExpression {
                                coneTypeOrNull = receiver.returnTypeRef.coneTypeOrNull
                                source = receiver.source
                                calleeReference = buildExplicitThisReference {
                                    source = receiver.source
                                    // labelName = receiver.labelName?.asString()
                                }
                            }
                        })

                        // TODO What is explicitReceiver?
                        // this.explicitReceiver

                        this.extensionReceiver = thisReceiverParameter?.let { thisReceiverParameter ->
                            buildThisReceiverExpression {
                                coneTypeOrNull = thisReceiverParameter.typeRef.coneTypeOrNull
                                source = thisReceiverParameter.source
                                calleeReference = buildImplicitThisReference {
                                    boundSymbol = thisReceiverParameter.symbol
                                }
                            }
                        }

                        if (thisValueParameters.isNotEmpty()) {
                            this.argumentList = buildResolvedArgumentList(
                                null,
                                mapping = linkedMapOf<FirExpression, FirValueParameter>().apply {
                                    thisValueParameters.forEachIndexed { index, thisParam ->
                                        val qualifiedAccess = thisParam.toQualifiedAccess()
                                        put(qualifiedAccess, originValueParameters[index])
                                    }
                                }
                            )
                        }
                    }
                }
            )

            this.typeRef = buildResolvedTypeRef {
                this.coneType = StandardClassIds.SuspendFunctionN(0)
                    .createConeType(session, arrayOf(originFunSymbol.resolvedReturnType))
            }
        }
        lambdaTarget.bind(lambda)

        this.statements.add(
            buildReturnExpression {
                this.target = newFunTarget
                this.result = buildFunctionCall {
                    this.coneTypeOrNull = returnTypeRef.coneType
                    this.source = originFunc.body?.source
                    this.calleeReference = buildResolvedNamedReference {
                        this.source = bridgeFunSymbol.source
                        this.name = bridgeFunSymbol.name
                        this.resolvedSymbol = bridgeFunSymbol
                    }

                    this.argumentList = buildResolvedArgumentList(
                        null,
                        mapping = linkedMapOf<FirExpression, FirValueParameter>().apply {
                            put(
                                buildAnonymousFunctionExpression {
                                    source = null
                                    anonymousFunction = lambda
                                    isTrailingLambda = false
                                },
                                //                                            funData.bridgeFunData.lambdaParameter
                                bridgeFunSymbol.valueParameterSymbols.first().fir
                            )

                            // scope, if exists
                            val valueParameterSymbols =
                                bridgeFunSymbol.valueParameterSymbols

                            if (valueParameterSymbols.size > 1) {
                                // 支持:
                                //  CoroutineScope? -> this as? CoroutineScope
                                //  CoroutineScope -> this or throw error
                                //  CoroutineScope (optional) -> this or ignore
                                //  Any -> this
                                //  index 1 以及后面的所有参数都进行处理

                                fun ConeKotlinType.isCoroutineScope(): Boolean {
                                    return isSubtypeOf(
                                        coroutineScopeSymbol.toLookupTag().constructClassType(),
                                        session
                                    )
                                }

                                fun thisReceiverExpression(): FirThisReceiverExpression {
                                    return buildThisReceiverExpression {
                                        coneTypeOrNull =
                                            originFunSymbol.dispatchReceiverType
                                        source = originFunSymbol.source
                                        calleeReference = buildImplicitThisReference {
                                            boundSymbol = owner
                                        }
                                    }
                                }

                                val listIterator = valueParameterSymbols.listIterator(1)
                                listIterator.forEach { parameterSymbol ->
                                    val parameterFir = parameterSymbol.fir
                                    val parameterType = parameterSymbol.resolvedReturnType

                                    val parameterTypeNotNullable = if (parameterType.isMarkedNullable) {
                                        parameterType.makeConeTypeDefinitelyNotNullOrNotNull(session.typeContext)
                                    } else {
                                        parameterType
                                    }

                                    when {
                                        // 参数是 CoroutineScope(?) 类型
                                        parameterTypeNotNullable.isCoroutineScope() -> {
                                            if (parameterType.isMarkedNullable) {
                                                // scope = this as? CoroutineScope
                                                put(
                                                    buildTypeOperatorCall {
                                                        source = originFunSymbol.source
                                                        coneTypeOrNull = parameterTypeNotNullable
                                                        argumentList = buildResolvedArgumentList(
                                                            null,
                                                            mapping = linkedMapOf<FirExpression, FirValueParameter>().apply {
                                                                put(thisReceiverExpression(), parameterFir)
                                                            }
                                                        )
                                                        operation = FirOperation.SAFE_AS
                                                        conversionTypeRef =
                                                            parameterTypeNotNullable.toFirResolvedTypeRef()
                                                    },
                                                    parameterFir
                                                )
                                            } else {
                                                // coroutine not nullable
                                                // put if this is `CoroutineScope` or it is optional, otherwise throw error
                                                var ownerIsCoroutineScopeOrParameterIsOptional =
                                                    parameterSymbol.hasDefaultValue
                                                for (superType in owner.getSuperTypes(session, recursive = false)) {
                                                    if (superType.isCoroutineScope()) {
                                                        put(thisReceiverExpression(), parameterFir)
                                                        ownerIsCoroutineScopeOrParameterIsOptional = true
                                                        break
                                                    }
                                                }

                                                // or throw error?
                                                if (!ownerIsCoroutineScopeOrParameterIsOptional) {
                                                    error(
                                                        "Owner is not a CoroutineScope, " +
                                                                "and the transformer function requires a `CoroutineScope` parameter."
                                                    )
                                                }
                                            }
                                        }

                                        // 参数是 Any(?) 类型
                                        parameterTypeNotNullable == session.builtinTypes.anyType.coneType -> {
                                            // 直接把 this 放进去，不需要转换
                                            put(thisReceiverExpression(), parameterFir)
                                        }
                                    }
                                }


                            }

                        }
                    )
                }
            }
        )
    }

    @OptIn(SymbolInternals::class)
    private fun generateSyntheticFunctions(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
        originFunSymbol: FirNamedFunctionSymbol,
        funData: SyntheticFunData,
        results: MutableList<FirNamedFunctionSymbol>,
    ) {
        val realBridgeFunSymbol =
            funData.transformerFunctionSymbol(transformerFunctionSymbolMap, ::findTransformerFunctionSymbol)

        val annotationData = funData.annotationData
        if (!annotationData.asProperty) {
            // Check the overridden for isOverride based on source function (func) 's overridden
            val isOverride =
                checkSyntheticFunctionIsOverrideBasedOnSourceFunction(funData, originFunSymbol, checkContext)

            // generate
            val originFunc = originFunSymbol.fir

            val (functionAnnotations, _, includeToOriginal) = copyAnnotations(originFunc, funData)

            val newFunSymbol = FirNamedFunctionSymbol(callableId)

            val key = SuspendTransformK2V3Key
            val originalTypeParameterCache = mutableListOf<CopiedTypeParameterPair>()

            val newFunTarget = FirFunctionTarget(null, isLambda = false)
            val newFun = buildSimpleFunctionCopy(originFunc) {
                origin = FirDeclarationOrigin.Plugin(SuspendTransformK2V3Key)
                source = originFunc.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                name = callableId.callableName
                symbol = newFunSymbol
                status = originFunc.status.copy(
                    isSuspend = false,
                    modality = originFunc.syntheticModifier,
                    // Use OPEN and `override` is unnecessary. .. ... Maybe?
                    isOverride = isOverride || isOverridable(
                        session,
                        callableId.callableName,
                        originFunc,
                        owner,
                        isProperty = false,
                    ),
                )

                // Copy the typeParameters.
                // Otherwise, in functions like the following, an error will occur
                // suspend fun <A> data(value: A): T = ...
                // Functions for which function-scoped generalizations (`<A>`) exist.
                // In the generated IR, data and dataBlocking will share an `A`, generating the error.
                // The error: Duplicate IR node
                //     [IR VALIDATION] JvmIrValidationBeforeLoweringPhase: Duplicate IR node: TYPE_PARAMETER name:A index:0 variance: superTypes:[kotlin.Any?] reified:false of FUN GENERATED[...]
                copyParameters(originalTypeParameterCache)

                // resolve returnType (with wrapped) after copyParameters
                returnTypeRef = resolveReturnType(funData, returnTypeRef, originalTypeParameterCache)

                val thisReceiverParameter = this.receiverParameter
                val thisContextParameters = this.contextParameters
                val thisValueParameters = this.valueParameters

                annotations.clear()
                annotations.addAll(functionAnnotations)

                body = generateSyntheticFunctionBody(
                    originFunc,
                    originFunSymbol,
                    owner,
                    thisContextParameters,
                    thisReceiverParameter,
                    newFunSymbol,
                    thisValueParameters,
                    realBridgeFunSymbol,
                    newFunTarget,
                    funData,
                    originalTypeParameterCache,
                    returnTypeRef
                )

                origin = key.origin
            }

            newFunTarget.bind(newFun)
            results.add(newFun.symbol)

            // 在原函数上附加的annotations
            originFunc.includeAnnotations(includeToOriginal)
        }
    }

    @OptIn(SymbolInternals::class)
    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()
        val originalTypeParameterCache: MutableList<CopiedTypeParameterPair> = mutableListOf()

        val funcMap = cache.getValue(FirCacheKey(owner, context.declaredScope))
            ?.get(callableId.callableName)
            ?: return emptyList()

        val propList = mutableListOf<FirPropertySymbol>()

        for ((originalFunSymbol, funData) in funcMap) {
            val annotationData = funData.annotationData

            if (!annotationData.asProperty) {
                continue
            }

            val isOverride =
                checkSyntheticFunctionIsOverrideBasedOnSourceFunction(funData, originalFunSymbol, checkContext)

            // generate
            val original = originalFunSymbol.fir

            val (functionAnnotations, propertyAnnotations, includeToOriginal) =
                copyAnnotations(original, funData)

            val pSymbol = FirPropertySymbol(callableId)

//                val pKey = SuspendTransformPluginKey(
//                    data = SuspendTransformUserDataFir(
//                        markerId = uniqueFunHash,
//                        originSymbol = original.symbol.asOriginSymbol(
//                            targetMarkerAnnotation,
//                            typeParameters = original.typeParameters,
//                            valueParameters = original.valueParameters,
//                            original.returnTypeRef.coneTypeOrNull?.classId,
//                            session
//                        ),
//                        asProperty = true,
//                        transformer = funData.transformer
//                    )
//                )
            val pKey = SuspendTransformK2V3Key

            val originalReturnType = original.returnTypeRef

            val copiedReturnType = originalReturnType.withReplacedConeType(
                originalReturnType.coneTypeOrNull?.copyConeTypeOrSelf(originalTypeParameterCache)
            )

            // copy完了再resolve，这样里面包的type parameter就不会有问题了（如果有type parameter的话）
            val resolvedReturnType = resolveReturnType(funData, copiedReturnType, originalTypeParameterCache)

            val newFunTarget = FirFunctionTarget(null, isLambda = false)

            val p1 = buildProperty {
                symbol = pSymbol
                name = callableId.callableName
                source = original.source?.fakeElement(KtFakeSourceElementKind.PluginGenerated)
                resolvePhase = original.resolvePhase
                moduleData = original.moduleData
                origin = pKey.origin
                attributes = original.attributes.copy()
                status = original.status.copy(
                    isSuspend = false,
                    isFun = false,
                    isInner = false,
                    modality = original.syntheticModifier,
                    isOverride = isOverride || isOverridable(
                        session,
                        callableId.callableName,
                        original,
                        owner,
                        isProperty = true
                    ),
                )

                isVar = false
                isLocal = false
                // Copy return type
                returnTypeRef = resolvedReturnType
                deprecationsProvider = UnresolvedDeprecationProvider //original.deprecationsProvider
                containerSource = original.containerSource
                dispatchReceiverType = original.dispatchReceiverType
                contextParameters.addAll(original.contextParameters)
//                contextReceivers.addAll(original.contextReceivers)
                // annotations
                annotations.addAll(propertyAnnotations)
                typeParameters.addAll(original.typeParameters)
                resolvePhase = FirResolvePhase.BODY_RESOLVE
                backingField = null
                bodyResolveState = FirPropertyBodyResolveState.NOTHING_RESOLVED

                getter = buildPropertyAccessor {
                    propertySymbol = pSymbol
                    val propertyAccessorSymbol = FirPropertyAccessorSymbol()
                    symbol = propertyAccessorSymbol
                    isGetter = true
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    moduleData = original.moduleData

                    // annotations
                    annotations.addAll(functionAnnotations)

                    returnTypeRef = resolvedReturnType
                    origin = pKey.origin

                    status = original.status.copy(
                        isSuspend = false,
                        isFun = false,
                        isInner = false,
                        modality = original.syntheticModifier,
                        isOverride = false, // funData.isOverride,
                        //                            visibility = this@buildProperty.status
                    )

                    valueParameters.addAll(original.valueParameters)

                    copyParameters(originalTypeParameterCache, false, propertyAccessorSymbol)

                    val thisValueParameters = this.valueParameters

                    body = generateSyntheticFunctionBody(
                        original,
                        originalFunSymbol,
                        owner,
                        emptyList(),
                        null,
                        propertyAccessorSymbol,
                        thisValueParameters,
                        funData.transformerFunctionSymbol(
                            transformerFunctionSymbolMap,
                            ::findTransformerFunctionSymbol
                        ),
                        newFunTarget,
                        funData,
                        originalTypeParameterCache,
                        returnTypeRef
                    )
                }.also { getter ->
                    newFunTarget.bind(getter)
                }
            }

            propList.add(p1.symbol)

            // 在原函数上附加的annotations
            original.includeAnnotations(includeToOriginal)

        }

        return propList
    }


    private fun checkSyntheticFunctionIsOverrideBasedOnSourceFunction(
        syntheticFunData: SyntheticFunData,
        func: FirNamedFunctionSymbol,
        checkContext: CheckerContext
    ): Boolean {
        // Check the overridden for isOverride based on source function (func) 's overridden
        var isOverride = false
        val annoData = syntheticFunData.annotationData
        val markAnnotation = syntheticFunData.transformer.markAnnotation

        if (func.isOverride && !isOverride) {
            // func.processOverriddenFunctionsSafe()
            func.processOverriddenFunctionsSafe(
                checkContext
            ) processOverridden@{ overriddenFunction ->
                if (!isOverride) {
                    // check parameters and receivers
                    val resolvedReceiverTypeRef = overriddenFunction.resolvedReceiverTypeRef
                    val originReceiverTypeRef = func.resolvedReceiverTypeRef

                    // origin receiver should be the same as symbol receiver
                    if (originReceiverTypeRef != resolvedReceiverTypeRef) {
                        return@processOverridden
                    }

                    // all value parameters should be a subtype of symbol's value parameters
                    val symbolParameterSymbols = overriddenFunction.valueParameterSymbols
                    val originParameterSymbols = func.valueParameterSymbols

                    if (symbolParameterSymbols.size != originParameterSymbols.size) {
                        return@processOverridden
                    }

                    for ((index, symbolParameter) in symbolParameterSymbols.withIndex()) {
                        val originParameter = originParameterSymbols[index]
                        if (
                            originParameter.resolvedReturnType != symbolParameter.resolvedReturnType
                        ) {
                            return@processOverridden
                        }
                    }

                    val overriddenAnnotation = firAnnotation(
                        overriddenFunction, markAnnotation, overriddenFunction.getContainingClassSymbol()
                    ) ?: return@processOverridden

                    val overriddenAnnoData = overriddenAnnotation.toTransformAnnotationData(
                        markAnnotation, overriddenFunction.name.asString()
                    )

                    // Same functionName, same asProperty, the generated synthetic function will be same too.
                    if (
                        overriddenAnnoData.functionName == annoData.functionName
                        && overriddenAnnoData.asProperty == annoData.asProperty
                    ) {
                        isOverride = true
                    }
                }
            }
        }
        return isOverride
    }

    // private val annotationPredicates
    //     get() = DeclarationPredicate.create {
    //         val annotationFqNames = suspendTransformConfiguration.transformers.values
    //             .flatMapTo(mutableSetOf()) { transformerList ->
    //                 transformerList.map { it.markAnnotation.fqName }
    //             }
    //
    //         hasAnnotated(annotationFqNames)
    //     }
    //
    // 在进行 #102 的时候，使用 registerPredicates { register(annotationPredicates) }
    // 会导致FIR中原函数的 mark annotation 中的范型 (如果有的话，以 `T` 为例) 无法被解析，产生如下错误：
    //  @R|love/forte/plugin/suspendtrans/annotation/JvmBlockingWithType<ERROR CLASS: Symbol not found for T>
    //  即: ERROR CLASS: Symbol not found for T
    // override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    //     register(annotationPredicates)
    // }

    private fun createCache(
        classSymbol: FirClassSymbol<*>,
        declaredScope: FirClassDeclaredMemberScope?,
        transformerFunctionSymbolMap: Map<Transformer, FirNamedFunctionSymbol>
    ): Map<Name, Map<FirNamedFunctionSymbol, SyntheticFunData>>? {
        if (declaredScope == null) return null

        val platform = classSymbol.moduleData.platform

        fun check(targetPlatform: TargetPlatform): Boolean {
            return checkPlatform(platform, targetPlatform)
        }

        // Key -> synthetic fun name
        // Value Map ->
        //      Key: -> origin fun symbol
        //      Values -> FunData
        val map = ConcurrentHashMap<Name, MutableMap<FirNamedFunctionSymbol, SyntheticFunData>>()

        val platformTransformers = suspendTransformConfiguration.transformers
            .filter { (platform, _) -> check(platform) }

        declaredScope.processAllFunctions { func ->
            if (!func.isSuspend) return@processAllFunctions

            val functionName = func.name.asString()

            platformTransformers
                .forEach { (_, transformerList) ->
                    for (transformer in transformerList) {
                        val markAnnotation = transformer.markAnnotation

                        val anno = firAnnotation(func, markAnnotation, classSymbol)
                            ?: continue

                        @OptIn(ExperimentalReturnTypeOverrideGenericApi::class)
                        val markAnnotationTypeArgument: FirTypeProjection? =
                            if (markAnnotation.hasReturnTypeOverrideGeneric) {
                                anno.typeArguments.firstOrNull()
                            } else {
                                null
                            }

                        // TODO 也许错误延后抛出，缓存里找不到的话即用即找，可以解决无法在当前模块下使用的问题？
                        //  see https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues/100
                        val transformerFunctionSymbol = transformerFunctionSymbolMap[transformer]

                        // 读不到注解的参数？
                        // 必须使用 anno.getXxxArgument(Name(argument name)),
                        // 使用 argumentMapping.mapping 获取不到结果
                        val annoData = anno.toTransformAnnotationData(markAnnotation, functionName)

                        val syntheticFunNameString = annoData.functionName

                        val syntheticFunName = Name.identifier(syntheticFunNameString)

                        map.computeIfAbsent(syntheticFunName) {
                            ConcurrentHashMap()
                        }[func] = SyntheticFunData(
                            syntheticFunName,
                            annoData,
                            transformer,
                            transformerFunctionSymbol,
                            markAnnotationTypeArgument,
                            platform
                        )
                    }
                }
        }

        return map
    }

    private fun FirAnnotation.toTransformAnnotationData(
        markAnnotation: MarkAnnotation,
        sourceFunctionName: String
    ) = TransformAnnotationData.of(
        session,
        firAnnotation = this,
        annotationBaseNamePropertyName = markAnnotation.baseNameProperty,
        annotationSuffixPropertyName = markAnnotation.suffixProperty,
        annotationAsPropertyPropertyName = markAnnotation.asPropertyProperty,
        annotationMarkNamePropertyName = markAnnotation.markNameProperty?.propertyName,
        defaultBaseName = sourceFunctionName,
        defaultSuffix = markAnnotation.defaultSuffix,
        defaultAsProperty = markAnnotation.defaultAsProperty,
    )

    /**
     * Find mark annotation from the function, and if not found, find from the class.
     */
    private fun firAnnotation(
        func: FirNamedFunctionSymbol,
        markAnnotation: MarkAnnotation,
        classSymbol: FirBasedSymbol<*>?
    ): FirAnnotation? {
        return func.resolvedAnnotationsWithArguments.getAnnotationsByClassId(
            markAnnotation.classId,
            session
        ).firstOrNull()
            ?: classSymbol?.resolvedAnnotationsWithArguments?.getAnnotationsByClassId(
                markAnnotation.classId,
                session
            )?.firstOrNull()
    }

    private fun resolveReturnType(
        funData: SyntheticFunData,
        returnTypeRef: FirTypeRef,
        originalTypeParameterCache: MutableList<CopiedTypeParameterPair>
    ): FirTypeRef {
        val transformer = funData.transformer
        val returnTypeArg = funData.markAnnotationTypeArgument

        val returnTypeConeType: ConeKotlinType = returnTypeArg?.toConeTypeProjection()?.let {
            // TODO if is star projection, use Any or Nothing? and the nullable?
            it.type
                ?.copyConeTypeOrSelf(originalTypeParameterCache)
                ?: session.builtinTypes.nullableAnyType.coneType
        } ?: returnTypeRef.coneType

        val resultConeType: ConeKotlinType = resolveReturnConeType(transformer, returnTypeConeType)

        return if (resultConeType is ConeErrorType) {
            buildErrorTypeRef {
                diagnostic = resultConeType.diagnostic
                coneType = resultConeType
            }
        } else {
            buildResolvedTypeRef {
                coneType = resultConeType
            }
        }
    }

    private fun resolveReturnConeType(
        transformer: Transformer,
        returnTypeConeType: ConeKotlinType
    ): ConeKotlinType {
        val returnType = transformer.transformReturnType
            ?: return returnTypeConeType // OrNull // original.symbol.resolvedReturnType

        var typeArguments: Array<ConeTypeProjection> = emptyArray()

        if (transformer.transformReturnTypeGeneric) {
            typeArguments = arrayOf(ConeKotlinTypeProjectionOut(returnTypeConeType))
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
        original: FirSimpleFunction,
        syntheticFunData: SyntheticFunData,
    ): CopyAnnotations {
        val transformer = syntheticFunData.transformer

        // val originalAnnotationClassIdMap = original.annotations.keysToMap { it.toAnnotationClassId(session) }
        val originalAnnotationClassIdMap = original.annotations.keysToMap { it.toAnnotationClassId(session) }

        val copyFunction = transformer.copyAnnotationsToSyntheticFunction
        val copyProperty = transformer.copyAnnotationsToSyntheticProperty

        val includeClassInfos = transformer.syntheticFunctionIncludeAnnotations.mapTo(mutableSetOf()) { it.classInfo }
        // 对于 all mark annotation class infos 来说，它们必须包括在 include class infos 中才能被拷贝
        // 将 all mark annotation class infos 添加到 excludes, 但是排除掉在 include 中包含的。
        val excludeMarkAnnotations = allMarkAnnotationClassInfos - includeClassInfos

        val excludes =
            transformer.copyAnnotationExcludes.map { it.toClassId() } + excludeMarkAnnotations.map { it.toClassId() }
        val includes = transformer.syntheticFunctionIncludeAnnotations.map { it.toInfo() }
        val markNameProperty = transformer.markAnnotation.markNameProperty

        val functionAnnotationList: List<FirAnnotation> = buildList {
            if (copyFunction) {
                val notCompileAnnotationsCopied = originalAnnotationClassIdMap.filterNot { (_, annotationClassId) ->
                    if (annotationClassId == null) return@filterNot true
                    excludes.any { ex -> annotationClassId == ex }
                }.keys

                /*
                 * Create a new annotation based the annotation from the original function.
                 * It will be crashed with `IllegalArgumentException: Failed requirement`
                 * when using the `notCompileAnnotationsCopied` directly
                 * if there have some arguments with type `KClass`,
                 * e.g. `annotation class OneAnnotation(val target: KClass<*>)` or `kotlin.OptIn`.
                 *
                 * See https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues/56
                 */
                val copied: List<FirAnnotation> = notCompileAnnotationsCopied.map { a ->
                    buildAnnotation {
                        annotationTypeRef = buildResolvedTypeRef {
                            coneType = a.resolvedType
                        }

                        this.typeArguments.addAll(a.typeArguments)
                        this.argumentMapping = buildAnnotationArgumentMapping {
                            this.source = a.source
                            this.mapping.putAll(a.argumentMapping.mapping)
                        }
                    }
                }

                addAll(copied)
            }

            // add includes
            includes.forEach { include ->
                if (include.classInfo in allMarkAnnotationClassInfos) {
                    // 如果 classId 在 allMarkAnnotationClassInfos 中，不直接添加
                    return@forEach
                }

                val classId = include.classId

                val includeAnnotation = buildAnnotation {
                    argumentMapping = buildAnnotationArgumentMapping()
                    annotationTypeRef = buildResolvedTypeRef {
                        coneType = classId.createConeType(session)
                    }
                }
                add(includeAnnotation)
            }

            if (markNameProperty != null) {
                // Add name marker annotation if it's possible
                val markName = syntheticFunData.annotationData.markName
                if (markName != null) {
                    // Find the marker annotation, e.g., JvmName
                    val markNameAnnotation = buildAnnotation {
                        argumentMapping = buildAnnotationArgumentMapping {
                            // org.jetbrains.kotlin.fir.java.JavaUtilsKt
                            val markNameArgument = buildLiteralExpression(
                                source = null,
                                kind = ConstantValueKind.String,
                                value = markName,
                                setType = true
                            )

                            val annotationMarkNamePropertyName = markNameProperty.annotationMarkNamePropertyName
                            mapping[Name.identifier(annotationMarkNamePropertyName)] = markNameArgument
                        }
                        val markNameAnnotationClassId = markNameProperty.annotation.toClassId()
                        annotationTypeRef = buildResolvedTypeRef {
                            coneType = markNameAnnotationClassId.createConeType(session)
                        }
                    }

                    add(markNameAnnotation)
                }
            }
        }

        val propertyAnnotationList: List<FirAnnotation> = buildList {
            if (copyProperty) {
                val notCompileAnnotationsCopied = originalAnnotationClassIdMap.filterNot { (_, annotationClassId) ->
                    if (annotationClassId == null) return@filterNot true
                    excludes.any { ex -> annotationClassId == ex }
                }.keys

                addAll(notCompileAnnotationsCopied)
            }

            // add includes
            includes
                .filter { it.includeProperty }
                .forEach { include ->
                    val classId = include.classId
                    val includeAnnotation = buildAnnotation {
                        argumentMapping = buildAnnotationArgumentMapping()
                        annotationTypeRef = buildResolvedTypeRef {
                            coneType = classId.createConeType(session)
                        }
                    }
                    add(includeAnnotation)
                }
        }

        // original annotations

        val infos = transformer.originFunctionIncludeAnnotations.map { it.toInfo() }

        val includeToOriginals: List<FirAnnotation> = infos
            .mapNotNull { (classId, _, repeatable, _) ->
                if (!repeatable) {
                    // 不能是已经存在的
                    if (originalAnnotationClassIdMap.values.any { it == classId }) {
                        return@mapNotNull null
                    }
                }

                buildAnnotation {
                    argumentMapping = buildAnnotationArgumentMapping()
                    annotationTypeRef = buildResolvedTypeRef {
                        coneType = classId.createConeType(session)
                    }
                }
            }

        return CopyAnnotations(functionAnnotationList, propertyAnnotationList, includeToOriginals)
    }

    private fun isOverridable(
        session: FirSession,
        functionName: Name,
        thisReceiverTypeRef: FirTypeRef?,
        thisValueTypeRefs: List<FirTypeRef?>,
        owner: FirClassSymbol<*>,
        isProperty: Boolean,
    ): Boolean {

        if (isProperty) {
            // value symbols must be empty.
            check(thisValueTypeRefs.isEmpty()) { "property's value parameters must be empty." }

            return owner.getSuperTypes(session)
                .asSequence()
                .mapNotNull {
                    it.toRegularClassSymbol(session)
                }
                .flatMap {
                    it.declaredProperties(session)
                    // it.declarationSymbols.filterIsInstance<FirPropertySymbol>()
                }
                .filter { !it.isFinal }
                .filter { it.callableId.callableName == functionName }
                // overridable receiver parameter.
                .filter {
                    thisReceiverTypeRef sameAs it.resolvedReceiverTypeRef
                }
                .any()
        } else {
            return owner.getSuperTypes(session)
                .asSequence()
                .mapNotNull {
                    it.toRegularClassSymbol(session)
                }
                .flatMap {
                    it.declaredFunctions(session)
                    // it.declarationSymbols.filterIsInstance<FirNamedFunctionSymbol>()
                }
                // not final, overridable
                .filter { !it.isFinal }
                // same name
                .filter { it.callableId.callableName == functionName }
                // overridable receiver parameter.
                .filter {
                    thisReceiverTypeRef sameAs it.resolvedReceiverTypeRef
                }
                // overridable value parameters
                .filter {
                    val valuePs = it.valueParameterSymbols
                        .map { vps ->
                            vps.resolvedReturnTypeRef
                        }

                    if (valuePs.size != thisValueTypeRefs.size) return@filter false

                    for (i in valuePs.indices) {
                        val valueP = valuePs[i]
                        val thisP = thisValueTypeRefs[i]

                        if (thisP notSameAs valueP) {
                            return@filter false
                        }
                    }

                    true
                }
                .any()
        }
    }

    private fun isOverridable(
        session: FirSession,
        functionName: Name,
        originFunc: FirSimpleFunction,
        owner: FirClassSymbol<*>,
        isProperty: Boolean = false,
    ): Boolean {
        // 寻找 owner 中所有的 open/abstract的,
        // parameters 的类型跟 originFunc 的 parameters 匹配的

        val thisReceiverTypeRef: FirTypeRef? = originFunc.receiverParameter?.typeRef

        val thisValueTypeRefs: List<FirTypeRef?> = originFunc.valueParameters.map {
            it.symbol.resolvedReturnTypeRef
        }

        return isOverridable(session, functionName, thisReceiverTypeRef, thisValueTypeRefs, owner, isProperty)
    }

    /**
     * Check is an overridable same type for a function's parameters.
     *
     */
    private infix fun FirTypeRef?.sameAs(otherSuper: FirTypeRef?): Boolean {
        if (this == otherSuper) return true
        val thisConeType = this?.coneTypeOrNull
        val otherConeType = otherSuper?.coneTypeOrNull

        if (thisConeType == otherConeType) return true

        if (thisConeType == null || otherConeType == null) {
            // One this null, other is not null
            return false
        }

        return thisConeType sameAs otherConeType
    }

    private infix fun ConeKotlinType.sameAs(otherSuper: ConeKotlinType): Boolean {
        return this == otherSuper
        // 有什么便捷的方法来处理 ConeTypeParameterType ？
    }

    private infix fun FirTypeRef?.notSameAs(otherSuper: FirTypeRef?): Boolean = !(this sameAs otherSuper)


    private fun ConeKotlinType.copyWithTypeParameters(
        parameters: List<CopiedTypeParameterPair>,
    ): ConeKotlinType? {
        fun findCopied(target: ConeKotlinType) = parameters.find { (original, _) ->
            original.toConeType() == target
        }?.copied

        val copiedThis = findCopied(this)
        if (copiedThis != null) {
            return copiedThis.toConeType()
        }

        when (this) {
            is ConeDynamicType -> {
            }

            is ConeFlexibleType -> {
            }

            is ConeClassLikeType -> {
                if (typeArguments.isNotEmpty()) {
                    fun mapProjection(projection: ConeTypeProjection): ConeTypeProjection? {
                        val findCopiedDirectly = projection.type?.let { type -> findCopied(type) }
                        if (findCopiedDirectly != null) {
                            return findCopiedDirectly.toConeType()
                        }

                        return when (projection) {
                            // is ConeFlexibleType -> { }

                            is ConeClassLikeType -> {
                                projection.copyWithTypeParameters(parameters)
                            }

                            is ConeCapturedType -> {
//                                val lowerType = projection.lowerType?.let { lowerType ->
//                                    findCopied(lowerType)
//                                }?.toConeType()

                                val lowerType = projection.lowerType?.copyWithTypeParameters(parameters)

                                if (lowerType == null) {
                                    projection.copy(lowerType = lowerType)
                                } else {
                                    null
                                }
                            }

                            is ConeDefinitelyNotNullType -> {
                                findCopied(projection.original)
                                    ?.toConeType()
                                    ?.let { projection.copy(it) }
                            }
                            // is ConeIntegerConstantOperatorType -> TODO()
                            // is ConeIntegerLiteralConstantType -> TODO()
                            is ConeIntersectionType -> {
                                val upperBoundForApproximation = projection.upperBoundForApproximation
                                    ?.copyWithTypeParameters(parameters)
//                                val upperBoundForApproximation =
//                                    projection.upperBoundForApproximation
//                                        ?.let { findCopied(it) }
//                                        ?.toConeType()

                                var anyIntersectedTypes = false

                                val intersectedTypes = projection.intersectedTypes.map { ktype ->
                                    findCopied(ktype)?.toConeType()
//                                    ktype.copyWithTypeParameters(parameters)
                                        ?.also { anyIntersectedTypes = true }
                                        ?: ktype
                                }

                                if (upperBoundForApproximation != null || anyIntersectedTypes) {
                                    ConeIntersectionType(
                                        intersectedTypes,
                                        upperBoundForApproximation
                                    )
                                } else {
                                    null
                                }
                            }
                            // is ConeLookupTagBasedType -> TODO()
                            // is ConeStubTypeForTypeVariableInSubtyping -> TODO()
                            // is ConeTypeVariableType -> TODO()
                            is ConeKotlinTypeConflictingProjection -> {
//                                findCopied(projection.type)
//                                    ?.toConeType()
//                                    ?.let { projection.copy(it) }

                                projection.type.copyWithTypeParameters(parameters)
                                    ?.let { projection.copy(it) }
                            }

                            is ConeKotlinTypeProjectionIn -> {
//                                findCopied(projection.type)
//                                    ?.toConeType()
//                                    ?.let { projection.copy(it) }

                                projection.type.copyWithTypeParameters(parameters)
                                    ?.let { projection.copy(it) }
                            }

                            is ConeKotlinTypeProjectionOut -> {
//                                findCopied(projection.type)
//                                    ?.toConeType()
//                                    ?.let { projection.copy(it) }

                                projection.type.copyWithTypeParameters(parameters)
                                    ?.let { projection.copy(it) }
                            }

                            is ConeTypeParameterType -> {
//                                findCopied(projection)?.toConeType()
                                projection.copyWithTypeParameters(parameters)
                            }

                            ConeStarProjection -> ConeStarProjection

                            // Other unknowns, e.g., ClassLike
                            else -> null
                        }
                    }

                    val typeArguments: Array<ConeTypeProjection> = typeArguments.map { projection ->
                        mapProjection(projection) ?: projection
                    }.toTypedArray()

                    return classId?.createConeType(
                        session = session,
                        typeArguments = typeArguments,
                        nullable = isMarkedNullable
                    )
                }

                if (isPrimitiveType()) {
                    return this
                }

                return classId?.createConeType(session = session, nullable = isMarkedNullable)
            }

            is ConeTypeParameterType -> {
                return findCopied(this)?.toConeType() ?: this
//                return parameters.find { (original, _) ->
//                    original.toConeType() == this
//                }?.copied?.toConeType() ?: this
            }

            else -> {
                // ?
            }
        }

        return null
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

