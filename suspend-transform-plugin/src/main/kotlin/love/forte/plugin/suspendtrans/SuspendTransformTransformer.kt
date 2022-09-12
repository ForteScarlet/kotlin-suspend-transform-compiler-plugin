package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.fileParent
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.JvmNames.JVM_SYNTHETIC_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.jvm.annotations.JVM_DEFAULT_FQ_NAME
import java.util.*
import org.jetbrains.kotlin.ir.util.isInterface as isInterfaceIr

private inline val IrPluginContext.isJvm: Boolean get() = platform?.isJvm() == true
private inline val IrPluginContext.isJs: Boolean get() = platform?.isJs() == true

/**
 *
 * @author ForteScarlet
 */
class SuspendTransformTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {
    
    private val generatedAnnotation = pluginContext.referenceClass(generatedAnnotationName)!!
    
    private val jvmRunBlockingFunctionOrNull =
        pluginContext.referenceFunctions(jvmRunInBlockingFunctionName).singleOrNull()
    
    private val jvmRunBlockingFunction
        get() = jvmRunBlockingFunctionOrNull ?: error("jvmRunBlockingFunction unsupported.")
    
    private val jvmRunAsyncFunctionOrNull = pluginContext.referenceFunctions(jvmRunInAsyncFunctionName).singleOrNull()
    
    private val jvmRunAsyncFunction
        get() = jvmRunAsyncFunctionOrNull ?: error("jvmRunAsyncFunction unsupported.")
    
    private val completableFutureClassOrNull = pluginContext.referenceClass(completableFutureClassName)
    
    private val completableFutureClass
        get() = completableFutureClassOrNull
            ?: throw NoSuchElementException("completableFutureClass: $completableFutureClassName")
    
    private val jsRunAsyncFunctionOrNull = pluginContext.referenceFunctions(jsRunInAsyncFunctionName).singleOrNull()
    
    private val jsRunAsyncFunction
        get() = jsRunAsyncFunctionOrNull ?: error("jsRunAsyncFunction unsupported.")
    
    private val jsPromiseClassOrNull = pluginContext.referenceClass(jsPromiseClassName)
    private val jsPromiseClass get() = jsPromiseClassOrNull ?: error("jsPromiseClass unsupported.")
    
    private fun FunctionDescriptor.isGenerated(): Boolean {
        return this.annotations.hasAnnotation(generatedAnnotationName)
    }
    
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun IrFunction.isGenerated(): Boolean {
        return descriptor.isGenerated()
    }
    
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun IrFunction.findAnnotationDescriptor(fqName: FqName): AnnotationDescriptor? {
        return descriptor.annotations.findAnnotation(fqName)
    }
    
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        resolveFunction(declaration)
        return super.visitFunctionNew(declaration)
    }
    
    
    private fun resolveFunction(
        function: IrFunction,
    ) {
        val parent = function.parent
    
        if (parent is IrClass || parent is IrFile) {
            var generated = false
            parent as IrDeclarationContainer
            if (function.isGenerated()) {
                return
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
            if (generated) {
                function.annotations = buildList {
                    addAll(function.annotations)
                    // +@Generated
                    add(
                        pluginContext.createIrBuilder(function.symbol)
                            .irAnnotationConstructor(generatedAnnotation)
                    )
                    if (pluginContext.isJvm) {
                        // +@JvmSynthetic
                        add(
                            pluginContext.createIrBuilder(function.symbol).irAnnotationConstructor(
                                pluginContext.referenceClass(
                                    JVM_SYNTHETIC_ANNOTATION_FQ_NAME
                                )!!
                            )
                        )
                    }
                }
            }
        }
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
    
    
    private fun AnnotationDescriptor?.functionName(
        baseNamePropertyName: String = "baseName",
        suffixPropertyName: String = "suffix",
        defaultBaseName: String, defaultSuffix: String,
    ): String {
        if (this == null) return "$defaultBaseName$defaultSuffix"
        
        val visitor = object : AbstractNullableAnnotationArgumentVoidDataVisitor<String>() {
            override fun visitStringValue(value: String): String = value
        }
        
        val baseName = argumentValue(baseNamePropertyName)?.accept(visitor, null)
        val suffix = argumentValue(suffixPropertyName)?.accept(visitor, null)
        
        return (baseName ?: defaultBaseName) + (suffix ?: defaultSuffix)
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
            plusAnnotations = {
                // @JvmDefault for interface
                if (originFunction.parentClassOrNull?.isInterface == true) {
                    it.add(
                        pluginContext.createIrBuilder(symbol).irAnnotationConstructor(
                            pluginContext.referenceClass(JVM_DEFAULT_FQ_NAME)!!
                        )
                    )
                }
            },
        )
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
            },
            plusAnnotations = {
                // @JvmDefault for interface
                if (originFunction.parentClassOrNull?.isInterface == true) {
                    it.add(
                        pluginContext.createIrBuilder(symbol).irAnnotationConstructor(
                            pluginContext.referenceClass(JVM_DEFAULT_FQ_NAME)!!
                        )
                    )
                }
            })
        
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
            },
            plusAnnotations = {
                // TODO @JsName?
            })
        
        return listOf(promiseFunction)
    }
}

private fun IrSimpleFunction.asProperty(): IrProperty {
    val baseFunction = this
    val parentClassOrFile = baseFunction.parentClassOrNull ?: baseFunction.fileParent
    val property = IrFactoryImpl.buildProperty {
        updateFrom(baseFunction)
        name = baseFunction.name
        startOffset = baseFunction.startOffset
        endOffset = baseFunction.endOffset
        origin = IrDeclarationOrigin.DEFINED
        returnType = originalFunction.returnType
        modality = parentClassOrFile.computeModality(baseFunction)
        visibility = if (parentClassOrFile.isInterface) DescriptorVisibilities.PUBLIC
        else baseFunction.visibility
        isExternal = false
        isExpect = false
        isVar = false
        isConst = false
    }.apply {
        this.copyAttributes(baseFunction)
        parent = parentClassOrFile
        getter = baseFunction
    }
    
    return property
}

private fun IrBuilderWithScope.irAnnotationConstructor(
    clazz: IrClassSymbol,
): IrConstructorCall {
    return run {
        irCall(clazz.constructors.first())
    }.run {
        irConstructorCall(this, this.symbol)
    }
}

private fun List<IrConstructorCall>.filterNotCompileAnnotations(): List<IrConstructorCall> = filterNot {
    it.type.isClassType(toJvmAsyncAnnotationName.toUnsafe()) || it.type.isClassType(toJvmBlockingAnnotationName.toUnsafe()) || it.type.isClassType(
        toJsPromiseAnnotationName.toUnsafe()
    )
}

private val IrPluginContext.generatedAnnotation get() = referenceClass(generatedAnnotationName)!!


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
    
    copyFunction.body = context.createIrBuilder(copyFunction.symbol).irBlockBody {
        val suspendLambda = context.createSuspendLambdaWithCoroutineScope(
            parent = copyFunction,
            // suspend () -> R
            lambdaType = context.symbols.suspendFunctionN(0).typeWith(copyFunction.returnType),
            originFunction = originFunction
        ).also { +it }
        
        +irReturn(irCall(transformTargetFunctionCall).apply {
            putValueArgument(0, irCall(suspendLambda.primaryConstructor!!).apply {
                for ((index, parameter) in copyFunction.paramsAndReceiversAsParamsList().withIndex()) {
                    putValueArgument(index, irGet(parameter))
                }
            })
        })
    }
    
    return copyFunction
}


private fun IrPluginContext.createIrBuilder(
    symbol: IrSymbol,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
) = DeclarationIrBuilder(this, symbol, startOffset, endOffset)


private fun IrType.isClassType(fqName: FqNameUnsafe, hasQuestionMark: Boolean? = null): Boolean {
    if (this !is IrSimpleType) return false
    if (hasQuestionMark != null && this.isMarkedNullable() != hasQuestionMark) return false
    return classifier.isClassWithFqName(fqName)
}

private fun IrDeclarationContainer.computeModality(originFunction: IrFunction): Modality {
    if (isInterface) return Modality.OPEN
    
    return when {
        originFunction.isFinal -> Modality.FINAL
        isOpen || isAbstract || isSealed -> Modality.OPEN
        else -> Modality.FINAL
    }
}

private val IrDeclarationContainer.isInterface: Boolean
    get() = (this as? IrClass)?.isInterfaceIr == true

private val IrDeclarationContainer.isAbstract: Boolean
    get() = (this as? IrClass)?.modality == Modality.ABSTRACT

private val IrDeclarationContainer.isOpen: Boolean
    get() = (this as? IrClass)?.modality == Modality.OPEN

private val IrDeclarationContainer.isSealed: Boolean
    get() = (this as? IrClass)?.modality == Modality.SEALED

// private val IrDeclarationContainer.isPrivate: Boolean
//     get() = (this as? IrDeclarationWithVisibility)?.visibility?.delegate == Visibilities.Private

private val IrFunction.isFinal: Boolean
    get() = (this as? IrSimpleFunction)?.modality == Modality.FINAL


/**
 * Generates an anonymous object.
 *
 * - extends `suspend () -> Unit`.
 * - takes dispatch and extension receivers as param, followed by normal value params, to the constructor of this object
 */
internal fun IrPluginContext.createSuspendLambdaWithCoroutineScope(
    parent: IrDeclarationParent,
    lambdaType: IrSimpleType,
    originFunction: IrFunction,
): IrClass {
    return IrFactoryImpl.buildClass {
        name = SpecialNames.NO_NAME_PROVIDED
        kind = ClassKind.CLASS
        // isInner = true
    }.apply clazz@{
        this.parent = parent
        superTypes = listOf(lambdaType)
        
        val fields = originFunction.paramsAndReceiversAsParamsList().map {
            addField(it.name.identifierOrMappedSpecialName.synthesizedName, it.type)
        }
        
        createImplicitParameterDeclarationWithWrappedDescriptor()
        
        addConstructor {
            isPrimary = true
        }.apply constructor@{
            val newParams = fields.associateWith { irField ->
                this@constructor.addValueParameter {
                    name = irField.name
                    type = irField.type
                }
            }
            
            this@constructor.body = createIrBuilder(symbol).irBlockBody {
                +irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.constructors.single())
                
                for ((irField, irValueParam) in newParams) {
                    +irSetField(irGet(this@clazz.thisReceiver!!), irField, irGet(irValueParam))
                }
            }
        }
        
        val irClass = this
        
        addFunction("invoke", lambdaType.arguments.last().typeOrNull!!, isSuspend = true).apply functionInvoke@{
            this.overriddenSymbols =
                listOf(irClass.superTypes[0].getClass()!!.functionsSequence.single { it.name.identifier == "invoke" && it.isOverridable }.symbol)
            
            // this.createDispatchReceiverParameter()
            this.body = createIrBuilder(symbol).run {
                // don't use expr body, coroutine codegen can't generate for it.
                irBlockBody {
                    +irCall(originFunction).apply call@{
                        // set arguments
                        
                        val arguments = fields.mapTo(LinkedList()) { it } // preserve order
                        
                        fun IrField.irGetField(): IrGetFieldImpl {
                            return irGetField(irGet(this@functionInvoke.dispatchReceiverParameter!!), this)
                        }
                        
                        if (originFunction.dispatchReceiverParameter != null) {
                            this@call.dispatchReceiver = arguments.pop().irGetField()
                        }
                        if (originFunction.extensionReceiverParameter != null) {
                            this@call.extensionReceiver = arguments.pop().irGetField()
                        }
                        
                        // this@call.putValueArgument(0, irGet(scopeParam))
                        for ((index, irField) in arguments.withIndex()) {
                            this@call.putValueArgument(index, irField.irGetField())
                        }
                    }
                }
            }
        }
    }
}

private fun IrFunction.paramsAndReceiversAsParamsList(): List<IrValueParameter> {
    return buildList {
        if (!isStatic) {
            dispatchReceiverParameter?.let(this::add)
        }
        extensionReceiverParameter?.let(this::add)
        valueParameters.let(this::addAll)
    }
}


private val Name.identifierOrMappedSpecialName: String
    get() {
        return when (this.asString()) {
            "<this>" -> "\$receiver" // finally synthesized as
            else -> this.identifier
        }
    }


private val IrDeclarationContainer.functionsSequence: Sequence<IrSimpleFunction>
    get() = declarations.asSequence().filterIsInstance<IrSimpleFunction>()