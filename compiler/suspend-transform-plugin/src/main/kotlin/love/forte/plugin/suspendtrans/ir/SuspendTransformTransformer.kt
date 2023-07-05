package love.forte.plugin.suspendtrans.ir

import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.SuspendTransformUserData
import love.forte.plugin.suspendtrans.SuspendTransformUserDataKey
import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.utils.*
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isAnnotationWithEqualFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformTransformer(
    private val configuration: SuspendTransformConfiguration,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {
//    private inline val isJvm: Boolean get() = pluginContext.platform?.isJvm() == true
//    private inline val isJs: Boolean get() = pluginContext.platform?.isJs() == true

    //    private val generatedAnnotation = pluginContext.referenceClass(generatedAnnotationName)!!
//    private val jvmRunBlockingFunctionName = configuration.jvm.jvmBlockingFunctionName
//    private val jvmRunAsyncFunctionName = configuration.jvm.jvmAsyncFunctionName

//    private val jvmRunBlockingCallableId: CallableId = jvmRunBlockingFunctionName?.callableId
//        ?: configuration.jvm.jvmBlockingFunctionInfo.let { blocking ->
//            CallableId(blocking.packageName.fqn, blocking.className?.fqn, Name.identifier(blocking.functionName))
//        }
//    private val jvmRunAsyncCallableId: CallableId = jvmRunAsyncFunctionName?.callableId
//        ?: configuration.jvm.jvmAsyncFunctionInfo.let { async ->
//            CallableId(async.packageName.fqn, async.className?.fqn, Name.identifier(async.functionName))
//        }
//
//    private val jsRunAsyncFunctionName = configuration.js.jsPromiseFunctionName
//    private val jsRunAsyncCallableId = jsRunAsyncFunctionName?.callableId
//        ?: configuration.js.jsPromiseFunctionInfo.let { promise ->
//            CallableId(promise.packageName.fqn, promise.className?.fqn, Name.identifier(promise.functionName))
//        }
//
//    private val jvmOriginIncludeAnnotations =
//        configuration.jvm.originFunctionIncludeAnnotations?.toList() ?: emptyList()
//    private val jsOriginIncludeAnnotations = configuration.js.originFunctionIncludeAnnotations.toList()
//
//    private val jvmRunBlockingFunctionOrNull =
////        pluginContext.referenceFunctions(jvmRunBlockingFunctionName.fqn).singleOrNull()
//        pluginContext.referenceFunctions(jvmRunBlockingCallableId).firstOrNull()
//
//    private val jvmRunBlockingFunction
//        get() = jvmRunBlockingFunctionOrNull
//            ?: error("jvmRunBlockingFunction ($jvmRunBlockingFunctionName) unsupported.")
//
//    private val jvmRunAsyncFunctionOrNull = pluginContext.referenceFunctions(jvmRunAsyncCallableId).firstOrNull()
//
//    private val jvmRunAsyncFunction
//        get() = jvmRunAsyncFunctionOrNull ?: error("jvmRunAsyncFunction ($jvmRunAsyncFunctionName) unsupported.")
//
//    private val jsRunAsyncFunctionOrNull = pluginContext.referenceFunctions(jsRunAsyncCallableId).firstOrNull()
//
//    private val jsRunAsyncFunction
//        get() = jsRunAsyncFunctionOrNull ?: error("jsRunAsyncFunction ($jsRunAsyncFunctionName) unsupported.")

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        resolveFunctionBodyByDescriptor(declaration, declaration.descriptor)

        return super.visitFunctionNew(declaration)
    }


    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        val getter = declaration.getter ?: return super.visitPropertyNew(declaration)
        resolveFunctionBodyByDescriptor(getter, declaration.descriptor)

        return super.visitPropertyNew(declaration)
    }

    private fun resolveFunctionBodyByDescriptor(declaration: IrFunction, descriptor: CallableDescriptor): IrFunction? {
        val userData = descriptor.getUserData(SuspendTransformUserDataKey) ?: return null
        val callableFunction =
            pluginContext.referenceFunctions(userData.transformer.transformFunctionInfo.toCallableId()).firstOrNull()
                ?: throw IllegalStateException("Transform function ${userData.transformer.transformFunctionInfo} not found")

        val generatedOriginFunction = resolveFunctionBody(declaration, userData.originFunction, callableFunction)

        if (generatedOriginFunction != null) {
            postProcessGenerateOriginFunction(generatedOriginFunction, userData)
        }

        return generatedOriginFunction
    }

    @OptIn(FirIncompatiblePluginAPI::class)
    private fun postProcessGenerateOriginFunction(function: IrFunction, userData: SuspendTransformUserData) {
        function.annotations = buildList {
            val currentAnnotations = function.annotations
            fun hasAnnotation(name: FqName): Boolean =
                currentAnnotations.any { a -> a.isAnnotationWithEqualFqName(name) }
            addAll(currentAnnotations)

            val syntheticFunctionIncludes = userData.transformer.originFunctionIncludeAnnotations

            syntheticFunctionIncludes.forEach { include ->
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
        originFunctionDescriptor: SimpleFunctionDescriptor,
        transformTargetFunctionCall: IrSimpleFunctionSymbol,
    ): IrFunction? {
        val parent = function.parent
        if (parent is IrDeclarationContainer) {
            val originFunctions = parent.declarations.filterIsInstance<IrFunction>()
                .filter { f -> f.descriptor == originFunctionDescriptor }

            if (originFunctions.size != 1) {
                // maybe override function
                /*
                    interface A {
                       @JvmBlocking suspend fun a(): Int
                    }

                    interface B : A {
                        // here
                        override suspend fun a(): Int = 1
                    }
                 */
                System.err.println(
                    "originFunctions.size should be 1, but ${originFunctions.size} (originFunctionDescriptor = $originFunctionDescriptor, findIn = ${(parent as? IrDeclaration)?.descriptor}, originFunctions = $originFunctions)"
                )
                return null
            }
//            require(originFunctions.size == 1) {
//            }

            val originFunction = originFunctions.first()

            function.body = null
            function.body = generateTransformBodyForFunction(
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
            parent = function,
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

            if (owner.valueParameters.size > 1) {
                val secondType = owner.valueParameters[1].type
                val coroutineScopeTypeName = "kotlinx.coroutines.CoroutineScope".fqn
                val coroutineScopeTypeClassId = ClassId.topLevel("kotlinx.coroutines.CoroutineScope".fqn)
                val coroutineScopeTypeNameUnsafe = coroutineScopeTypeName.toUnsafe()
                if (secondType.isClassType(coroutineScopeTypeNameUnsafe)) {
                    function.dispatchReceiverParameter?.also { dispatchReceiverParameter ->
                        context.referenceClass(coroutineScopeTypeClassId)?.also { coroutineScopeRef ->
//                            irGet(dispatchReceiverParameter)
//                            irAs(irGet(dispatchReceiverParameter), coroutineScopeRef).safeAs<>()
                            if (dispatchReceiverParameter.type.isSubtypeOfClass(coroutineScopeRef)) {
                                // put 'this' to second arg
                                putValueArgument(1, irGet(dispatchReceiverParameter))
                            } else {
                                // TODO scope safe cast
                                val scopeType = coroutineScopeRef.defaultType
//                                irAs(irGet(dispatchReceiverParameter), coroutineScopeRef.defaultType)

                                val irSafeAs = IrTypeOperatorCallImpl(
                                    startOffset,
                                    endOffset,
                                    scopeType,
                                    IrTypeOperator.SAFE_CAST,
                                    scopeType,
                                    irGet(dispatchReceiverParameter)
                                )

                                putValueArgument(1, irSafeAs)
                            }
                        }
                    }
                }

            }

        })
    }
}
