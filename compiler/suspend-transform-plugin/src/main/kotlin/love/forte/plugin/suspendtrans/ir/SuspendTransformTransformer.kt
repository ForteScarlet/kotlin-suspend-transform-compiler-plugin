package love.forte.plugin.suspendtrans.ir

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.utils.createIrBuilder
import love.forte.plugin.suspendtrans.utils.createSuspendLambdaWithCoroutineScope
import love.forte.plugin.suspendtrans.utils.irAnnotationConstructor
import love.forte.plugin.suspendtrans.utils.paramsAndReceiversAsParamsList
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isAnnotationWithEqualFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformTransformer(
    configuration: SuspendTransformConfiguration,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {
    private inline val isJvm: Boolean get() = pluginContext.platform?.isJvm() == true
    private inline val isJs: Boolean get() = pluginContext.platform?.isJs() == true

    //    private val generatedAnnotation = pluginContext.referenceClass(generatedAnnotationName)!!
    private val jvmRunBlockingFunctionName = configuration.jvm.jvmBlockingFunctionName
    private val jvmRunAsyncFunctionName = configuration.jvm.jvmAsyncFunctionName
    private val jsRunAsyncFunctionName = configuration.js.jsPromiseFunctionName
    private val jvmOriginIncludeAnnotations = configuration.jvm.originFunctionIncludeAnnotations.toList()
    private val jsOriginIncludeAnnotations = configuration.js.originFunctionIncludeAnnotations.toList()

    private val jvmRunBlockingFunctionOrNull =
        pluginContext.referenceFunctions(jvmRunBlockingFunctionName.fqn).singleOrNull()

    private val jvmRunBlockingFunction
        get() = jvmRunBlockingFunctionOrNull
            ?: error("jvmRunBlockingFunction ($jvmRunBlockingFunctionName) unsupported.")

    private val jvmRunAsyncFunctionOrNull = pluginContext.referenceFunctions(jvmRunAsyncFunctionName.fqn).singleOrNull()

    private val jvmRunAsyncFunction
        get() = jvmRunAsyncFunctionOrNull ?: error("jvmRunAsyncFunction ($jvmRunAsyncFunctionName) unsupported.")

    private val jsRunAsyncFunctionOrNull = pluginContext.referenceFunctions(jsRunAsyncFunctionName.fqn).singleOrNull()

    private val jsRunAsyncFunction
        get() = jsRunAsyncFunctionOrNull ?: error("jsRunAsyncFunction ($jsRunAsyncFunctionName) unsupported.")

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
        val generatedOriginFunction = when {
            descriptor.getUserData(ToJvmBlocking) != null -> resolveFunctionBody(
                declaration,
                descriptor.getUserData(ToJvmBlocking)!!.originFunction,
                jvmRunBlockingFunction
            )

            descriptor.getUserData(ToJvmAsync) != null -> resolveFunctionBody(
                declaration,
                descriptor.getUserData(ToJvmAsync)!!.originFunction,
                jvmRunAsyncFunction
            )

            descriptor.getUserData(ToJsAsync) != null -> resolveFunctionBody(
                declaration,
                descriptor.getUserData(ToJsAsync)!!.originFunction,
                jsRunAsyncFunction
            )

            else -> null
            //else -> resolveFunction(declaration)
        }

        if (generatedOriginFunction != null) {
            postProcessGenerateOriginFunction(generatedOriginFunction)
        }

        return generatedOriginFunction
    }

    private fun postProcessGenerateOriginFunction(function: IrFunction) {
        function.annotations = buildList {
            val currentAnnotations = function.annotations
            fun hasAnnotation(name: FqName): Boolean =
                currentAnnotations.any { a -> a.isAnnotationWithEqualFqName(name) }
            addAll(currentAnnotations)

            val includes = when {
                isJvm -> jvmOriginIncludeAnnotations
                isJs -> jsOriginIncludeAnnotations
                else -> emptyList()
            }

            includes.forEach { include ->
                val name = include.name.fqn
                val annotationClass = pluginContext.referenceClass(name) ?: return@forEach
                if (!include.repeatable && hasAnnotation(name)) {
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

            require(originFunctions.size == 1)

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
        })
    }
}
