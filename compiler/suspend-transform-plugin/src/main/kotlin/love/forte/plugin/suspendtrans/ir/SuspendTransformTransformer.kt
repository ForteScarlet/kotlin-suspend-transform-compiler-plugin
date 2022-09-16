package love.forte.plugin.suspendtrans.ir

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.utils.*
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.*
import org.jetbrains.kotlin.backend.jvm.ir.fileParent
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.jvm.annotations.JVM_DEFAULT_FQ_NAME

private inline val IrPluginContext.isJvm: Boolean get() = platform?.isJvm() == true
private inline val IrPluginContext.isJs: Boolean get() = platform?.isJs() == true

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformTransformer(
    configuration: SuspendTransformConfiguration,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {
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

    private val completableFutureClassOrNull = pluginContext.referenceClass(completableFutureClassName)

    private val completableFutureClass
        get() = completableFutureClassOrNull
            ?: throw NoSuchElementException("completableFutureClass: $completableFutureClassName")

    private val jsRunAsyncFunctionOrNull = pluginContext.referenceFunctions(jsRunAsyncFunctionName.fqn).singleOrNull()

    private val jsRunAsyncFunction
        get() = jsRunAsyncFunctionOrNull ?: error("jsRunAsyncFunction ($jsRunAsyncFunctionName) unsupported.")

    private val jsPromiseClassOrNull = pluginContext.referenceClass(jsPromiseClassName)
    private val jsPromiseClass get() = jsPromiseClassOrNull ?: error("jsPromiseClass unsupported.")

    private fun FunctionDescriptor.hasGenerated(): Boolean {
        return this.annotations.hasAnnotation(generatedAnnotationName)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun IrFunction.hasGenerated(): Boolean {
        return descriptor.hasGenerated()
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun IrFunction.findAnnotationDescriptor(fqName: FqName): AnnotationDescriptor? {
        return descriptor.annotations.findAnnotation(fqName)
    }

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
                pluginContext.isJvm -> jvmOriginIncludeAnnotations
                pluginContext.isJs -> jsOriginIncludeAnnotations
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

    private fun resolveFunction(
        function: IrFunction,
    ): IrFunction? {
        val parent = function.parent

        if (parent is IrClass || parent is IrFile) {
            var generated = false
            parent as IrDeclarationContainer
            if (function.hasGenerated()) {
                return null
            }
            when {
                pluginContext.isJvm -> {
                    val generatedList = generateJvmFunctions(function)
                    generatedList.forEach {
                        it.parent = parent
                        parent.declarations.add(it)
                    }
                    if (generatedList.isNotEmpty()) {
                        generated = true
                    }
                }

                pluginContext.isJs -> {
                    val generatedList = generateJsFunctions(function)
                    generatedList.forEach {
                        it.parent = parent
                        parent.declarations.add(it)
                    }
                    if (generatedList.isNotEmpty()) {
                        generated = true
                    }
                }
            }

            return if (generated) function else null
        }

        return null
    }

    private fun checkFunctionExists(function: IrFunction, functionName: String): Boolean {
        val parent = function.parentClassOrNull ?: function.fileParent
        return parent.functionsSequence.any { f ->
            if (f.name.asString() != functionName) {
                return@any false
            }
            if (f.allParametersCount != function.allParametersCount) {
                return@any false
            }
            val originFunctionParameters = function.allParameters
            f.allParameters.forEachIndexed { index, parameter ->
                val targetOriginFunctionParameter = originFunctionParameters[index]
                if (targetOriginFunctionParameter.type != parameter.type) {
                    return@any false
                }
            }

            true
        }
    }

    private fun generateJvmFunctions(originFunction: IrFunction): List<IrDeclaration> {
        return buildList {
            val blockingAnnotation = originFunction.findAnnotationDescriptor(toJvmBlockingAnnotationName)
            val blockingFunctionName = blockingAnnotation.functionName(
                defaultBaseName = originFunction.name.toString(), defaultSuffix = "Blocking"
            )
            if (originFunction.hasAnnotation(toJvmBlockingAnnotationName) && !checkFunctionExists(
                    originFunction, blockingFunctionName
                )
            ) {
                addAll(generateJvmBlockingFunction(originFunction, blockingFunctionName))
            }

            val asyncAnnotation = originFunction.findAnnotationDescriptor(toJvmAsyncAnnotationName)
            val asyncFunctionName =
                asyncAnnotation.functionName(defaultBaseName = originFunction.name.toString(), defaultSuffix = "Async")
            if (originFunction.hasAnnotation(toJvmAsyncAnnotationName) && !checkFunctionExists(
                    originFunction, asyncFunctionName
                )
            ) {
                addAll(generateJvmAsyncFunction(originFunction, asyncFunctionName))
            }
        }
    }

    private fun generateJsFunctions(originFunction: IrFunction): List<IrDeclaration> {
        val promiseAnnotation = originFunction.findAnnotationDescriptor(toJsPromiseAnnotationName)
        val promiseFunctionName =
            promiseAnnotation.functionName(defaultBaseName = originFunction.name.toString(), defaultSuffix = "Async")

        if (originFunction.hasAnnotation(toJsPromiseAnnotationName) && !checkFunctionExists(
                originFunction, promiseFunctionName
            )
        ) {
            return generateJsPromiseFunction(originFunction, promiseFunctionName)
        }

        return listOf()
    }

    private fun generateJvmBlockingFunction(
        originFunction: IrFunction,
        functionName: String,
    ): List<IrDeclaration> {
        val function = copyFunctionForGenerate(
            originFunction = originFunction,
            context = pluginContext,
            transformTargetFunctionCall = jvmRunBlockingFunction,
            builder = {
                name = Name.identifier(functionName)
            },
        ) {
            // @JvmDefault for interface
            if (originFunction.parentClassOrNull?.isInterface == true) {
                it.add(
                    pluginContext.createIrBuilder(symbol).irAnnotationConstructor(
                        pluginContext.referenceClass(JVM_DEFAULT_FQ_NAME)!!
                    )
                )
            }
        }
        val blockingAnnotation = originFunction.findAnnotationDescriptor(toJvmBlockingAnnotationName)
        val asProperty = blockingAnnotation?.argumentValue("asProperty")
            ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.booleanOnly, null) ?: false

        if (asProperty) {
            return listOf(function.asProperty())
        }


        return listOf(function)
    }

    private fun generateJvmAsyncFunction(
        originFunction: IrFunction,
        functionName: String,
    ): List<IrDeclaration> {
        val function = copyFunctionForGenerate(originFunction = originFunction,
            context = pluginContext,
            transformTargetFunctionCall = jvmRunAsyncFunction,
            builder = {
                name = Name.identifier(functionName)
                returnType = completableFutureClass.typeWith(originFunction.returnType)
            }
        ) {
            // @JvmDefault for interface
            if (originFunction.parentClassOrNull?.isInterface == true) {
                it.add(
                    pluginContext.createIrBuilder(symbol).irAnnotationConstructor(
                        pluginContext.referenceClass(JVM_DEFAULT_FQ_NAME)!!
                    )
                )
            }
        }

        val asyncAnnotation = originFunction.findAnnotationDescriptor(toJvmAsyncAnnotationName)
        val asProperty = asyncAnnotation?.argumentValue("asProperty")
            ?.accept(AbstractNullableAnnotationArgumentVoidDataVisitor.booleanOnly, null) ?: false

        if (asProperty) {
            return listOf(function.asProperty())
        }

        return listOf(function)
    }

    private fun generateJsPromiseFunction(originFunction: IrFunction, functionName: String): List<IrDeclaration> {
        val promiseFunction = copyFunctionForGenerate(originFunction = originFunction,
            context = pluginContext,
            transformTargetFunctionCall = jsRunAsyncFunction,
            builder = {
                name = Name.identifier(functionName)
                returnType = jsPromiseClass.typeWith(originFunction.returnType)
            }
        ) {
            // TODO @JsName?
        }

        return listOf(promiseFunction)
    }
}


private inline fun copyFunctionForGenerate(
    originFunction: IrFunction,
    context: IrPluginContext,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
    builder: IrFunctionBuilder.() -> Unit = {},
    plusAnnotations: IrSimpleFunction.(list: MutableList<IrConstructorCall>) -> Unit = {},
): IrSimpleFunction {
    // val originFunction = this
    val parentClassOrFile = originFunction.parentClassOrNull ?: originFunction.fileParent
    val copyFunction = IrFactoryImpl.buildFun {
        startOffset = originFunction.startOffset
        endOffset = originFunction.endOffset
        origin = IrDeclarationOrigin.DEFINED
        returnType = originFunction.returnType
        modality = parentClassOrFile.computeModality(originFunction)
        visibility = if (parentClassOrFile.isInterface) DescriptorVisibilities.PUBLIC
        else originFunction.visibility

        isSuspend = false
        isExternal = false
        isExpect = false

        builder()
    }

    copyFunction.parent = originFunction.parent
    copyFunction.extensionReceiverParameter = originFunction.extensionReceiverParameter?.copyTo(copyFunction)
    copyFunction.dispatchReceiverParameter = if (originFunction.isStatic) null
    else originFunction.dispatchReceiverParameter?.copyTo(copyFunction)

    copyFunction.copyAttributes(originFunction as IrAttributeContainer)
    copyFunction.copyParameterDeclarationsFrom(originFunction)

    copyFunction.annotations = buildList {
        addAll(originFunction.annotations.filterNotCompileAnnotations())
        add(context.createIrBuilder(copyFunction.symbol).irAnnotationConstructor(context.generatedAnnotation))
        copyFunction.plusAnnotations(this)
    }

    copyFunction.body = generateTransformBodyForFunction(
        context, copyFunction, originFunction, transformTargetFunctionCall
    )

    return copyFunction
}

private fun generateTransformBodyForFunction(
    context: IrPluginContext,
    function: IrFunction,
    originFunction: IrFunction,
    transformTargetFunctionCall: IrSimpleFunctionSymbol,
): IrBody {
    return context.createIrBuilder(function.symbol).irBlockBody {
        val suspendLambda = context.createSuspendLambdaWithCoroutineScope(
            parent = function,
            // suspend () -> R
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
