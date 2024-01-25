package love.forte.plugin.suspendtrans.ir

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.fir.SuspendTransformPluginKey
import love.forte.plugin.suspendtrans.utils.*
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSourceLocation
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.fileEntry
import org.jetbrains.kotlin.ir.util.isAnnotationWithEqualFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformTransformer(
    private val configuration: SuspendTransformConfiguration,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    // TODO What should be used in K2?
    private val reporter = kotlin.runCatching {
        // error: "This API is not supported for K2"
        pluginContext.createDiagnosticReporter(PLUGIN_REPORT_ID)
    }.getOrNull()

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        resolveFunctionBodyByDescriptor(declaration, declaration.descriptor)

        return super.visitFunctionNew(declaration)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        val getter = declaration.getter ?: return super.visitPropertyNew(declaration)
        resolveFunctionBodyByDescriptor(getter, declaration.descriptor, declaration)

        return super.visitPropertyNew(declaration)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun resolveFunctionBodyByDescriptor(
        declaration: IrFunction,
        descriptor: CallableDescriptor,
        property: IrProperty? = null
    ): IrFunction? {
        // K2
        val pluginKey = if (property != null) {
            // from property
            (property.origin as? IrDeclarationOrigin.GeneratedByPlugin)
                ?.pluginKey as? SuspendTransformPluginKey
        } else {
            (declaration.origin as? IrDeclarationOrigin.GeneratedByPlugin)
                ?.pluginKey as? SuspendTransformPluginKey
        }

        // K1 ?
        val userData = descriptor.getUserData(SuspendTransformUserDataKey)

        val generatedOriginFunction: IrFunction? = when {
            pluginKey != null -> {
                val callableFunction =
                    pluginContext.referenceFunctions(pluginKey.data.transformer.transformFunctionInfo.toCallableId())
                        .firstOrNull()
                        ?: throw IllegalStateException("Transform function ${pluginKey.data.transformer.transformFunctionInfo} not found")

                resolveFunctionBody(
                    declaration,
                    { f ->
                        pluginKey.data.originSymbol.checkSame(f)
                    },
                    callableFunction
                )?.also { generatedOriginFunction ->
                    if (property != null) {
                        // NO! BACKING! FIELD!
                        property.backingField = null
                    }
                    postProcessGenerateOriginFunction(
                        generatedOriginFunction,
                        pluginKey.data.transformer.originFunctionIncludeAnnotations
                    )
                }
            }

            userData != null -> {
                val callableFunction =
                    pluginContext.referenceFunctions(userData.transformer.transformFunctionInfo.toCallableId())
                        .firstOrNull()
                        ?: throw IllegalStateException("Transform function ${userData.transformer.transformFunctionInfo} not found")

                resolveFunctionBody(
                    declaration,
                    { f -> f.descriptor == userData.originFunction },
                    callableFunction
                )?.also { generatedOriginFunction ->
                    postProcessGenerateOriginFunction(
                        generatedOriginFunction,
                        userData.transformer.originFunctionIncludeAnnotations
                    )
                }
            }

            else -> return null
        }

        return generatedOriginFunction
    }

    private fun postProcessGenerateOriginFunction(
        function: IrFunction,
        originFunctionIncludeAnnotations: List<IncludeAnnotation>
    ) {
        function.annotations = buildList {
            val currentAnnotations = function.annotations
            fun hasAnnotation(name: FqName): Boolean =
                currentAnnotations.any { a -> a.isAnnotationWithEqualFqName(name) }
            addAll(currentAnnotations)

            originFunctionIncludeAnnotations.forEach { include ->
                val classId = include.classInfo.toClassId()
                val annotationClass = pluginContext.referenceClass(classId) ?: return@forEach
                if (!include.repeatable && hasAnnotation(classId.asSingleFqName())) {
                    return@forEach
                }

                add(pluginContext.createIrBuilder(function.symbol).irAnnotationConstructor(annotationClass))
            }
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun resolveFunctionBody(
        function: IrFunction,
        checkIsOriginFunction: (IrFunction) -> Boolean,
        transformTargetFunctionCall: IrSimpleFunctionSymbol,
    ): IrFunction? {
        val parent = function.parent
        if (parent is IrDeclarationContainer) {
            val originFunctions = parent.declarations.filterIsInstance<IrFunction>()
                .filter { checkIsOriginFunction(it) }

            if (originFunctions.size != 1) {
                val message =
                    "Synthetic function $function (${function.name}) 's originFunctions.size should be 1, but ${originFunctions.size} (findIn = ${(parent as? IrDeclaration)?.descriptor}, originFunctions = $originFunctions)"

                val location = when (val sourceLocation =
                    function.getSourceLocation(runCatching { function.fileEntry }.getOrNull())) {
                    is SourceLocation.Location -> {
                        IrMessageLogger.Location(
                            filePath = sourceLocation.file,
                            line = sourceLocation.line,
                            column = sourceLocation.column
                        )
                    }

                    else -> null
                }

                if (reporter != null) {
                    reporter.report(
                        IrMessageLogger.Severity.WARNING,
                        message,
                        location
                    )
                } else {
                    // TODO In K2?
                    System.err.println(message)
                }


                return null
            }

            val originFunction = originFunctions.first()

            function.body = generateTransformBodyForFunctionLambda(
                pluginContext,
                function,
                originFunction,
                transformTargetFunctionCall
            )

            return originFunction
        }



        return null
    }
}


private fun generateTransformBodyForFunction(
    context: IrPluginContext,
    function: IrFunction,
    originFunction: IrFunction,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
): IrBody {
    // default params
    val originValueParameters = originFunction.valueParameters
    function.valueParameters.forEachIndexed { index, parameter ->
        val originFunctionValueParameter = originValueParameters[index]
        parameter.defaultValue = originFunctionValueParameter.defaultValue
    }

    return context.createIrBuilder(function.symbol).irBlockBody {
        val suspendLambda = context.createSuspendLambdaWithCoroutineScope(
            parent = originFunction.parent,
            // suspend () -> ?
            lambdaType = context.symbols.suspendFunctionN(0).typeWith(originFunction.returnType),
            originFunction = originFunction
        ).also { +it }

        +irReturn(irCall(transformTargetFunctionCall).apply {
            putValueArgument(0, irCall(suspendLambda.primaryConstructor!!).apply {
                for ((index, parameter) in function.paramsAndReceiversAsParamsList().withIndex()) {
                    putValueArgument(index, irGet(parameter))
                }
            })
            // argument: 1, if is CoroutineScope, and this is CoroutineScope.
            //println("transformTargetFunctionCall.owner: ${transformTargetFunctionCall.owner}")
            //println(transformTargetFunctionCall.owner.valueParameters)
            val owner = transformTargetFunctionCall.owner

            // CoroutineScope
            val ownerValueParameters = owner.valueParameters

            if (ownerValueParameters.size > 1) {
                for (index in 1..ownerValueParameters.lastIndex) {
                    val valueParameter = ownerValueParameters[index]
                    val type = valueParameter.type
                    tryResolveCoroutineScopeValueParameter(type, context, function, owner, this@irBlockBody, index)
                }
            }

        })
    }
}

/**
 * new
 *
 */
private fun generateTransformBodyForFunctionLambda(
    context: IrPluginContext,
    function: IrFunction,
    originFunction: IrFunction,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
): IrBody {
    val originValueParameters = originFunction.valueParameters
    function.valueParameters.forEachIndexed { index, parameter ->
        val originFunctionValueParameter = originValueParameters[index]
        parameter.defaultValue = originFunctionValueParameter.defaultValue
    }

    return context.createIrBuilder(function.symbol).irBlockBody {
        val suspendLambdaFunc = context.createSuspendLambdaFunctionWithCoroutineScope(
            originFunction = originFunction,
            function = function
        )

        val lambdaType = context.symbols.suspendFunctionN(0).typeWith(suspendLambdaFunc.returnType)

        +irReturn(irCall(transformTargetFunctionCall).apply {
            putValueArgument(
                0,
                IrFunctionExpressionImpl(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    lambdaType,
                    suspendLambdaFunc,
                    IrStatementOrigin.LAMBDA
                )
            )
            // argument: 1, if is CoroutineScope, and this is CoroutineScope.
            val owner = transformTargetFunctionCall.owner

            // CoroutineScope
            val ownerValueParameters = owner.valueParameters

            if (ownerValueParameters.size > 1) {
                for (index in 1..ownerValueParameters.lastIndex) {
                    val valueParameter = ownerValueParameters[index]
                    val type = valueParameter.type
                    tryResolveCoroutineScopeValueParameter(type, context, function, owner, this@irBlockBody, index)
                }
            }

        })
    }
}

private val coroutineScopeTypeName = "kotlinx.coroutines.CoroutineScope".fqn
private val coroutineScopeTypeClassId = ClassId.topLevel("kotlinx.coroutines.CoroutineScope".fqn)
private val coroutineScopeTypeNameUnsafe = coroutineScopeTypeName.toUnsafe()

/**
 * 解析类型为 CoroutineScope 的参数。
 * 如果当前参数类型为 CoroutineScope:
 * - 如果当前 receiver 即为 CoroutineScope 类型，将其填充
 * - 如果当前 receiver 不是 CoroutineScope 类型，但是此参数可以为 null，
 *   则使用 safe-cast 将 receiver 转化为 CoroutineScope ( `dispatcher as? CoroutineScope` )
 * - 其他情况忽略此参数（适用于此参数有默认值的情况）
 */
private fun IrCall.tryResolveCoroutineScopeValueParameter(
    type: IrType,
    context: IrPluginContext,
    function: IrFunction,
    owner: IrSimpleFunction,
    builderWithScope: IrBuilderWithScope,
    index: Int
) {
    if (!type.isClassType(coroutineScopeTypeNameUnsafe)) {
        return
    }

    function.dispatchReceiverParameter?.also { dispatchReceiverParameter ->
        context.referenceClass(coroutineScopeTypeClassId)?.also { coroutineScopeRef ->
            if (dispatchReceiverParameter.type.isSubtypeOfClass(coroutineScopeRef)) {
                // put 'this' to the arg
                putValueArgument(index, builderWithScope.irGet(dispatchReceiverParameter))
            } else {
                val scopeType = coroutineScopeRef.defaultType

                val scopeParameter = owner.valueParameters.getOrNull(1)

                if (scopeParameter?.type?.isNullable() == true) {
                    val irSafeAs = IrTypeOperatorCallImpl(
                        startOffset,
                        endOffset,
                        scopeType,
                        IrTypeOperator.SAFE_CAST,
                        scopeType,
                        builderWithScope.irGet(dispatchReceiverParameter)
                    )

                    putValueArgument(index, irSafeAs)
                }
//                                irAs(irGet(dispatchReceiverParameter), coroutineScopeRef.defaultType)
            }
        }
    }
}
