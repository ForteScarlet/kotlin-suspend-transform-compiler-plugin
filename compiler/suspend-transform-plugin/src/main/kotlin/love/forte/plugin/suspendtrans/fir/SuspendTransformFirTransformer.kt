package love.forte.plugin.suspendtrans.fir

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.utils.CopyAnnotationsData
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.toClassId
import love.forte.plugin.suspendtrans.utils.toInfo
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.context.MutableCheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.processOverriddenFunctions
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.buildSingleExpressionBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.*
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
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private data class CopiedTypeParameterPair(
    val original: FirTypeParameter,
    val copied: FirTypeParameter
)

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformFirTransformer(
    session: FirSession,
    private val suspendTransformConfiguration: SuspendTransformConfiguration
) : FirDeclarationGenerationExtension(session) {
    private val targetMarkerValueName = Name.identifier("value")
    private val targetMarkerAnnotation = suspendTransformConfiguration.targetMarker?.toClassId()

    private data class FirCacheKey(
        val classSymbol: FirClassSymbol<*>,
        val memberScope: FirClassDeclaredMemberScope?
    )

    private data class FunData(
        val annotationData: TransformAnnotationData,
        val transformer: Transformer,
    )

    //    private val cache: FirCache<Pair<FirClassSymbol<*>, FirClassDeclaredMemberScope?>, Map<Name, Map<FirNamedFunctionSymbol, FunData>>?, Nothing?> =
    private val cache: FirCache<FirCacheKey, Map<Name, Map<FirNamedFunctionSymbol, FunData>>?, Nothing?> =
        session.firCachesFactory.createCache { (symbol, scope), c ->
            createCache(symbol, scope)
        }


    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val names = mutableSetOf<Name>()


        cache.getValue(FirCacheKey(classSymbol, context.declaredScope))?.forEach { (_, map) ->
            map.values.forEach { names.add(Name.identifier(it.annotationData.functionName)) }
        }

        return names
    }

    private val scope = ScopeSession()
    private val holder = SessionHolderImpl(session, scope)
    private val checkContext = MutableCheckerContext(
        holder,
        ReturnTypeCalculatorForFullBodyResolve.Default,
    )
    private val expectActualContext = session.expectActualMatchingContextFactory.create(session, scope)

    /**
     * 根据函数的名称、参数列表的各参数的类型计算一个hash字符串。
     */
    private fun FirSimpleFunction.calculateOriginFuncHash(): String {
        val str = buildString {
            append(name.asString())
            append(dispatchReceiverType.toString())
            append(receiverParameter?.typeRef?.coneTypeOrNull.toString())
            valueParameters.forEach { vp ->
                append(vp.returnTypeRef.coneTypeOrNull.toString())
            }
        }

        return Base64.getEncoder().encodeToString(str.toByteArray())
    }

    private fun FirSimpleFunctionBuilder.copyParameters() {
        val funSymbol = symbol
        val originalTypeParameterCache = mutableListOf<CopiedTypeParameterPair>()

        val newTypeParameters = typeParameters.map {
            buildTypeParameterCopy(it) {
                containingDeclarationSymbol = funSymbol // it.containingDeclarationSymbol
//                             symbol = it.symbol // FirTypeParameterSymbol()
                symbol = FirTypeParameterSymbol()
            }.also { new ->
                originalTypeParameterCache.add(CopiedTypeParameterPair(it, new))
            }
        }
        typeParameters.clear()
        typeParameters.addAll(newTypeParameters)

        val newValueParameters = valueParameters.map { vp ->
            buildValueParameterCopy(vp) {
                symbol = FirValueParameterSymbol(vp.symbol.name)

                val copiedConeType = vp.returnTypeRef.coneTypeOrNull
                    ?.copyWithTypeParameters(originalTypeParameterCache)

                if (copiedConeType != null) {
                    returnTypeRef = returnTypeRef.withReplacedConeType(copiedConeType)
                }
            }
        }
        valueParameters.clear()
        valueParameters.addAll(newValueParameters)

        receiverParameter?.also { receiverParameter ->
            receiverParameter.typeRef.coneTypeOrNull
                ?.copyWithTypeParameters(originalTypeParameterCache)
                ?.also { foundCopied ->
                    this.receiverParameter = buildReceiverParameterCopy(receiverParameter) {
                        typeRef = typeRef.withReplacedConeType(foundCopied)
                    }
                }
        }

        val coneTypeOrNull = returnTypeRef.coneTypeOrNull
        if (coneTypeOrNull != null) {
            returnTypeRef = returnTypeRef.withReplacedConeType(
                coneTypeOrNull
                    .copyWithTypeParameters(originalTypeParameterCache)
                    ?: coneTypeOrNull,
            )
        }
    }

//    private fun FirSimpleFunction.copySyntheticFun(
//        owner: FirClassSymbol<*>,
//        callableId: CallableId
//    ): FirSimpleFunction {
//        val origin = this
//
//        val marker = buildValueParameter {
//            source = origin.source
//            moduleData = origin.moduleData
//            this.origin = origin.origin
//            returnTypeRef = buildResolvedTypeRef {
//                coneType = StandardTypes.Int
//                source = null
//            }
//            name = Name.identifier("__test_marker")
//            symbol = FirValueParameterSymbol(name)
//            defaultValue = buildLiteralExpression(null, ConstantValueKind.Int, 1, null, setType = true)
//            containingFunctionSymbol = origin.symbol
//            backingField = null
//            isCrossinline = false
//            isNoinline = false
//            isVararg = false
//        }
//
//        val syntheticFun = buildSimpleFunctionCopy(this) {
//            this.origin = FirDeclarationOrigin.Synthetic.FakeFunction
//            name = callableId.callableName
//            symbol = FirNamedFunctionSymbol(callableId)
//            status = origin.status.copy(
//                modality = Modality.OPEN,
//                isOverride = false,
//                visibility = Visibilities.Public
//            )
//
//            copyParameters()
//
//            this.valueParameters.add(0, marker)
//
//            val builder = this
//
//            body = buildSingleExpressionBlock(
//                buildReturnExpression {
//                    target = FirFunctionTarget(null, false)
//                    result = buildFunctionCall {
//                        source = origin.source
//                        calleeReference = buildResolvedNamedReference {
//                            source = origin.source
//                            name = origin.name
//                            resolvedSymbol = origin.symbol
//                        }
//                        // TODO contextReceiverArguments
//
//                        argumentList = buildResolvedArgumentList(
//                            null,
//                            builder.valueParameters.associateByTo(LinkedHashMap()) { valueParameter ->
//                                buildCallableReferenceAccess {
//                                    source = valueParameter.source
//                                    calleeReference = buildResolvedNamedReference {
//                                        source = valueParameter.source
//                                        name = valueParameter.name
//                                        resolvedSymbol = valueParameter.symbol
//                                    }
//                                    coneTypeOrNull = valueParameter.returnTypeRef.coneTypeOrNull
//                                }
//                            })
//                    }
//                }
//            )
//
//        }
//
//        syntheticFun.excludeFromJsExport(session)
//        syntheticFun.jvmSynthetic(session)
//
//        return syntheticFun
//    }

    private fun FirSimpleFunction.appendTargetMarker(markerId: String) {
        if (targetMarkerAnnotation != null) {
            if (annotations.none {
                    it.fqName(session) == targetMarkerAnnotation.asSingleFqName()
                            && (it.argumentMapping.mapping[targetMarkerValueName] as? FirLiteralExpression)
                        ?.value == markerId
                }) {
                replaceAnnotations(
                    buildList {
                        addAll(annotations)
                        add(buildAnnotation {
                            argumentMapping = buildAnnotationArgumentMapping {
                                mapping[Name.identifier("value")] = buildLiteralExpression(
                                    null,
                                    ConstantValueKind.String,
                                    markerId,
                                    null,
                                    true,
                                    null
                                )
                            }
                            annotationTypeRef = buildResolvedTypeRef {
                                coneType = targetMarkerAnnotation.createConeType(session)
                            }
                        })
                    }
                )
            }
        }

    }

    @OptIn(SymbolInternals::class)
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

            val annotationData = funData.annotationData
            if (!annotationData.asProperty) {
                // Check the overridden for isOverride based on source function (func) 's overridden
                val isOverride = checkSyntheticFunctionIsOverrideBasedOnSourceFunction(funData, func, checkContext)

                // generate

                val originFunc = func.fir

                val uniqueFunHash = originFunc.calculateOriginFuncHash()

                // TODO 生成一个合成函数用来代替 originFunc, 然后其他生成的桥接函数都使用这个合成的函数而不是源函数。

//                val syntheticFunCallableName = callableId.callableName.asString() + "-f-${uniqueFunHash}"
//                val syntheticFunName = callableId.copy(Name.identifier(syntheticFunCallableName))
//                val syntheticFun = originFunc.copySyntheticFun(owner, callableId)
//                funList.add(syntheticFun.symbol)

                val (functionAnnotations, _) = copyAnnotations(originFunc, funData)

                val newFunSymbol = FirNamedFunctionSymbol(callableId)

                val key = SuspendTransformPluginKey(
                    data = SuspendTransformUserDataFir(
                        markerId = uniqueFunHash,
                        originSymbol = originFunc.symbol.asOriginSymbol(
                            targetMarkerAnnotation,
                            typeParameters = originFunc.typeParameters,
                            valueParameters = originFunc.valueParameters,
                            originFunc.returnTypeRef.coneTypeOrNull?.classId,
                            session,
                        ),
                        asProperty = false,
                        transformer = funData.transformer
                    )
                )

                val funTarget = FirFunctionTarget(null, false)

                val newFun = buildSimpleFunctionCopy(originFunc) {
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
                    // TODO copy to value parameters, receiver and return type?
                    copyParameters()

                    annotations.clear()
                    annotations.addAll(functionAnnotations)

                    val returnType = resolveReturnType(originFunc, funData, returnTypeRef)
                    returnTypeRef = returnType

                    body = buildBlock {
                        this.source = originFunc.body?.source

                        // lambda: () -> T
                        val lambdaTarget = FirFunctionTarget(null, isLambda = true)
                        val lambda = buildAnonymousFunction {
                            this.isLambda = true
                            this.moduleData = originFunc.moduleData
                            this.origin = key.origin
                            this.returnTypeRef = originFunc.returnTypeRef
                            this.hasExplicitParameterList = false
                            this.status = FirResolvedDeclarationStatusImpl.DEFAULT_STATUS_FOR_SUSPEND_FUNCTION_EXPRESSION
                            this.symbol = FirAnonymousFunctionSymbol()
                            this.body = buildSingleExpressionBlock(
                                buildReturnExpression {
                                    target = lambdaTarget
                                    result = buildFunctionCall {
                                        // Call original fun
                                        this.source = originFunc.source
                                        this.calleeReference = buildResolvedNamedReference {
                                            this.source = originFunc.source
                                            this.name = originFunc.name
                                            this.resolvedSymbol = originFunc.symbol
                                        }
//                                                this.argumentList = buildArgumentList {
//                                                    for (originParam in originFunc.valueParameters) {
//                                                        arguments.add(originParam.toQualifiedAccess())
//                                                    }
//                                                }
                                        this.argumentList = buildResolvedArgumentList(
                                            null,
                                            mapping = linkedMapOf<FirExpression, FirValueParameter>().apply {
                                                for (originParam in originFunc.valueParameters) {
                                                    put(originParam.toQualifiedAccess(), originParam)
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                        }
                        lambdaTarget.bind(lambda)

                        this.statements.add(buildAnonymousFunctionExpression {
                            this.anonymousFunction = lambda
                            this.isTrailingLambda = false
                        })

//                        this.statements.add(
//                            buildReturnExpression {
//                                this.target = funTarget
//                                val transformName = with(funData.transformer.transformFunctionInfo) {
//                                    buildString {
//                                        if (packageName.isNotEmpty()) {
//                                            append(packageName)
//                                            append('.')
//                                        }
//                                        if (className?.isNotEmpty() == true) {
//                                            append(className)
//                                            append('.')
//                                        }
//                                        append(functionName)
//                                    }
//                                }
//
//                                holder.session.moduleData.dependencies
//
//                                this.result = buildFunctionCall {
//                                    this.source = null
                                      // TODO 必须 resolved
//                                    this.calleeReference = buildSimpleNamedReference {
//                                        this.source = null
//                                        this.name = Name.identifier(transformName)
//                                    }
                                        // TODO Argument 必须 resolved
////                                    this.argumentList = buildArgumentList {
////                                        arguments.add(buildAnonymousFunctionExpression {
////                                            this.source = lambda.source
////                                            this.anonymousFunction = lambda
////                                            this.isTrailingLambda = false
////                                        })
////                                        // TODO scope?
////                                    }
//                                }
//                            }
//                        )
                        // ConeClassLikeType()
                    }
                    // body = null

                    // TODO 是否可以在FIR中就完成Body的构建？
//                    buildBlock {
//                        // build lambda
//                        val lambda = buildAnonymousFunctionExpression {
//                            anonymousFunction = buildAnonymousFunction {
//                                moduleData = originFunc.moduleData
//                                origin = key.origin
//                                status = FirResolvedDeclarationStatusImpl.DEFAULT_STATUS_FOR_SUSPEND_FUNCTION_EXPRESSION
//                                returnTypeRef = originFunc.returnTypeRef
//                                isLambda = true
//                                body = buildSingleExpressionBlock(
//                                    buildReturnExpression {
//                                        result = buildFunctionCall {
////                                            explicitReceiver = this@createConventionCall
//                                            calleeReference = buildResolvedNamedReference {
//                                                source = originFunc.source
//                                                name = originFunc.name
//                                                resolvedSymbol = originFunc.symbol
//                                            }
//
//                                            argumentList = buildResolvedArgumentList(null,
//                                                this@buildSimpleFunctionCopy.valueParameters.associateByTo(LinkedHashMap()) { valueParameter ->
//                                                    buildCallableReferenceAccess {
//                                                        source = valueParameter.source
//                                                        calleeReference = buildResolvedNamedReference {
//                                                            source = valueParameter.source
//                                                            name = valueParameter.name
//                                                            resolvedSymbol = valueParameter.symbol
//                                                        }
//                                                        coneTypeOrNull = valueParameter.returnTypeRef.coneTypeOrNull
//                                                    }
//                                                }
//                                                )
//                                        }
//                                    }
//                                )
//                                symbol = FirAnonymousFunctionSymbol()
//                            }
//                        }
//                        lambda
//
//
//                        /*
//                            fun xxxBlock() = transformFun({ orignFun }, )
//                         */
//                        statements.add(
//                            buildReturnExpression {
//
//
//                            }
//                        )
//
//                        buildFunctionCall {
//                            this
//                        }
//                    }

                    origin = key.origin
                }

                funTarget.bind(newFun)

                if (targetMarkerAnnotation != null) {
                    originFunc.appendTargetMarker(uniqueFunHash)
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
        val funcMap = cache.getValue(FirCacheKey(owner, context.declaredScope))
            ?.get(callableId.callableName)
            ?: return emptyList()

        val propList = mutableListOf<FirPropertySymbol>()

        funcMap.forEach { (func, funData) ->
            val annotationData = funData.annotationData
            if (annotationData.asProperty) {
                val isOverride = checkSyntheticFunctionIsOverrideBasedOnSourceFunction(funData, func, checkContext)

                // generate
                val original = func.fir

                val uniqueFunHash = original.calculateOriginFuncHash()

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
                        markerId = uniqueFunHash,
                        originSymbol = original.symbol.asOriginSymbol(
                            targetMarkerAnnotation,
                            typeParameters = original.typeParameters,
                            valueParameters = original.valueParameters,
                            original.returnTypeRef.coneTypeOrNull?.classId,
                            session
                        ),
                        asProperty = true,
                        transformer = funData.transformer
                    )
                )


                val returnType = resolveReturnType(original, funData, original.returnTypeRef)

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
                            isOverride = false, // funData.isOverride,
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

                if (targetMarkerAnnotation != null) {
                    original.appendTargetMarker(uniqueFunHash)
                }
            }
        }

        return propList
    }

    private fun checkSyntheticFunctionIsOverrideBasedOnSourceFunction(
        funData: FunData,
        func: FirNamedFunctionSymbol,
        checkContext: CheckerContext
    ): Boolean {
        // Check the overridden for isOverride based on source function (func) 's overridden
        var isOverride = false
        val annoData = funData.annotationData
        val markAnnotation = funData.transformer.markAnnotation

        if (func.isOverride && !isOverride) {
            func.processOverriddenFunctions(
                checkContext
            ) processOverridden@{ overriddenFunction ->
                if (!isOverride) {
                    // check parameters and receivers
                    val symbolReceiver = overriddenFunction.receiverParameter
                    val originReceiver = func.receiverParameter

                    // origin receiver should be the same as symbol receiver
                    if (originReceiver?.typeRef != symbolReceiver?.typeRef) {
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

                        val anno = firAnnotation(func, markAnnotation, classSymbol)
                            ?: continue


                        // 读不到注解的参数？
                        // 必须使用 anno.getXxxArgument(Name(argument name)),
                        // 使用 argumentMapping.mapping 获取不到结果
//                        println("RAW AnnoData: ${anno.argumentMapping.mapping}")

                        val annoData = anno.toTransformAnnotationData(markAnnotation, functionName)

                        val syntheticFunName = Name.identifier(annoData.functionName)
                        map.computeIfAbsent(syntheticFunName) { ConcurrentHashMap() }[func] =
                            FunData(annoData, transformer)
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
        defaultBaseName = sourceFunctionName,
        defaultSuffix = markAnnotation.defaultSuffix,
        defaultAsProperty = markAnnotation.defaultAsProperty,
    )

    private fun firAnnotation(
        func: FirNamedFunctionSymbol,
        markAnnotation: MarkAnnotation,
        classSymbol: FirBasedSymbol<*>?
    ) = func.resolvedAnnotationsWithArguments.getAnnotationsByClassId(
        markAnnotation.classId,
        session
    ).firstOrNull()
        ?: classSymbol?.resolvedAnnotationsWithArguments?.getAnnotationsByClassId(
            markAnnotation.classId,
            session
        )?.firstOrNull()

    private fun resolveReturnType(
        original: FirSimpleFunction,
        funData: FunData,
        returnTypeRef: FirTypeRef
    ): FirTypeRef {
        val resultConeType = resolveReturnConeType(original, funData, returnTypeRef)

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
        original: FirSimpleFunction,
        funData: FunData,
        returnTypeRef: FirTypeRef
    ): ConeKotlinType {
        val transformer = funData.transformer
        val returnType = transformer.transformReturnType
            ?: return returnTypeRef.coneType // OrNull // original.symbol.resolvedReturnType

        var typeArguments: Array<ConeTypeProjection> = emptyArray()

        if (transformer.transformReturnTypeGeneric) {
            typeArguments = arrayOf(ConeKotlinTypeProjectionOut(returnTypeRef.coneType))
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

        val (copyFunction, copyProperty, excludes, includes) = CopyAnnotationsData(
            transformer.copyAnnotationsToSyntheticFunction,
            transformer.copyAnnotationsToSyntheticProperty,
            transformer.copyAnnotationExcludes.map { it.toClassId() },
            transformer.syntheticFunctionIncludeAnnotations.map { it.toInfo() }
        )

        val functionAnnotationList = buildList<FirAnnotation> {
            if (copyFunction) {
                val notCompileAnnotationsCopied = original.annotations.filterNot {
                    val annotationClassId = it.toAnnotationClassId(session) ?: return@filterNot true
                    annotationClassId == targetMarkerAnnotation || excludes.any { ex -> annotationClassId == ex }
                }

                /*
                 * Create a new annotation based the annotation from the original function.
                 * It will be crashed with `IllegalArgumentException: Failed requirement`
                 * when using the `notCompileAnnotationsCopied` directly
                 * if there have some arguments with type `KClass`,
                 * e.g. `annotation class OneAnnotation(val target: KClass<*>)` or `kotlin.OptIn`.
                 *
                 * See https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues/56
                 */
                val copied = notCompileAnnotationsCopied.map { a ->
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
                        coneType = classId.createConeType(session)
                    }
                }
                add(includeAnnotation)
            }
        }

        val propertyAnnotationList = buildList<FirAnnotation> {
            if (copyProperty) {
                val notCompileAnnotationsCopied = original.annotations.filterNot {
                    val annotationClassId = it.toAnnotationClassId(session) ?: return@filterNot true
                    excludes.any { ex -> annotationClassId == ex }
                }

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

        return functionAnnotationList to propertyAnnotationList
    }


    private fun FirAnnotationArgumentMappingBuilder.includeGeneratedArguments(function: FirSimpleFunction) {
        fun MutableList<FirExpression>.addString(value: String) {
            val expression = buildLiteralExpression(
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
                if (this@typeString is ConeClassLikeType && typeArguments.isNotEmpty()) {
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
                    it.declarationSymbols.filterIsInstance<FirPropertySymbol>()
                }
                .filter { !it.isFinal }
                .filter { it.callableId.callableName == functionName }
                // overridable receiver parameter.
                .filter {
                    thisReceiverTypeRef sameAs it.receiverParameter?.typeRef
                }
                .any()
        } else {
            return owner.getSuperTypes(session)
                .asSequence()
                .mapNotNull {
                    it.toRegularClassSymbol(session)
                }
                .flatMap {
                    it.declarationSymbols.filterIsInstance<FirNamedFunctionSymbol>()
                }
                // not final, overridable
                .filter { !it.isFinal }
                // same name
                .filter { it.callableId.callableName == functionName }
                // overridable receiver parameter.
                .filter {
                    thisReceiverTypeRef sameAs it.receiverParameter?.typeRef
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

        when (this) {
            is ConeDynamicType -> {
                //println("Dynamic type: $this")
            }

            is ConeFlexibleType -> {
                //println("Flexible type: $this")
            }

            // var typeArguments: Array<ConeTypeProjection> = emptyArray()
            //
            //         if (transformer.transformReturnTypeGeneric) {
            //             typeArguments = arrayOf(ConeKotlinTypeProjectionOut(original.returnTypeRef.coneType))
            //         }

            is ConeClassLikeType -> {
                if (typeArguments.isNotEmpty()) {

                    fun mapProjection(projection: ConeTypeProjection): ConeTypeProjection? {
                        return when (projection) {
                            // is ConeFlexibleType -> {
                            // }
                            is ConeCapturedType -> {
                                val lowerType = projection.lowerType?.let { lowerType ->
                                    findCopied(lowerType)
                                }?.toConeType()

                                if (lowerType == null) {
                                    projection.copy(
                                        lowerType = lowerType
                                    )
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
                                val upperBoundForApproximation =
                                    projection.upperBoundForApproximation
                                        ?.let { findCopied(it) }
                                        ?.toConeType()

                                var anyIntersectedTypes = false

                                val intersectedTypes = projection.intersectedTypes.map { ktype ->
                                    findCopied(ktype)
                                        ?.toConeType()
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
                            // is ConeTypeVariableType -> {
                            //     TODO()
                            // }
                            is ConeKotlinTypeConflictingProjection -> {
                                findCopied(projection.type)
                                    ?.toConeType()
                                    ?.let { projection.copy(it) }
                            }

                            is ConeKotlinTypeProjectionIn -> {
                                findCopied(projection.type)
                                    ?.toConeType()
                                    ?.let { projection.copy(it) }
                            }

                            is ConeKotlinTypeProjectionOut -> {
                                findCopied(projection.type)
                                    ?.toConeType()
                                    ?.let { projection.copy(it) }
                            }

                            is ConeTypeParameterType -> {
                                findCopied(projection)?.toConeType()
                            }

                            ConeStarProjection -> projection
                            // Other unknowns
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
                    // typeArguments.forEach { projection ->
                    //     projection.type?.copyWithTypeParameters(parameters)
                    // }
                }

                return null
            }

            is ConeTypeParameterType -> {
                return parameters.find { (original, _) ->
                    original.toConeType() == this
                }?.copied?.toConeType() ?: this
            }

            else -> {

            }
        }
        return this
        // this.fullyExpandedClassId().createConeType()
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
