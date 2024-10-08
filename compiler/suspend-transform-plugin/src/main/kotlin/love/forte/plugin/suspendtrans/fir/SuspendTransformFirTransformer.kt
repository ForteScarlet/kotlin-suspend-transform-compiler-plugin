package love.forte.plugin.suspendtrans.fir

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.utils.CopyAnnotationsData
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import love.forte.plugin.suspendtrans.utils.toClassId
import love.forte.plugin.suspendtrans.utils.toInfo
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.context.MutableCheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.processOverriddenFunctions
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.collectUpperBounds
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildProperty
import org.jetbrains.kotlin.fir.declarations.builder.buildPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunctionCopy
import org.jetbrains.kotlin.fir.declarations.builder.buildTypeParameterCopy
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
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
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.SessionHolderImpl
import org.jetbrains.kotlin.fir.resolve.getSuperTypes
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculatorForFullBodyResolve
import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
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
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author ForteScarlet
 */
class SuspendTransformFirTransformer(
    session: FirSession,
    private val suspendTransformConfiguration: SuspendTransformConfiguration
) : FirDeclarationGenerationExtension(session) {

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

    @OptIn(SymbolInternals::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        val funcMap = cache.getValue(FirCacheKey(owner, context.declaredScope))
            ?.get(callableId.callableName)
            ?: return emptyList()

        val funList = mutableListOf<FirNamedFunctionSymbol>()

        funcMap.forEach { (func, funData) ->

            val annotationData = funData.annotationData
            if (!annotationData.asProperty) {
                // Check the overridden for isOverride based on source function (func) 's overridden
                val isOverride = checkSyntheticFunctionIsOverrideBasedOnSourceFunction(funData, func, checkContext)

                // generate

                val originFunc = func.fir

                val (functionAnnotations, _) = copyAnnotations(originFunc, funData)

                val newFunSymbol = FirNamedFunctionSymbol(callableId)

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
                     typeParameters.replaceAll {
                         buildTypeParameterCopy(it) {
                             symbol = FirTypeParameterSymbol()
                         }
                     }

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
                        overriddenFunction, markAnnotation, overriddenFunction.getContainingClassSymbol(session)
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
                    excludes.any { ex -> annotationClassId == ex }
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
                            type = a.resolvedType
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
                        type = classId.createConeType(session)
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
                            type = classId.createConeType(session)
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
