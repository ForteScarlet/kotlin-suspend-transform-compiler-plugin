package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.*
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.fileParent
import org.jetbrains.kotlin.descriptors.*
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
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.platform.jvm.isJvm
import java.util.*
import org.jetbrains.kotlin.ir.util.isInterface as isInterfaceIr

/**
 *
 * @author ForteScarlet
 */
public class SuspendTransformTransformer(
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
    
    private fun List<IrConstructorCall>.filterNotCompileAnnotations(): List<IrConstructorCall> =
        filterNot {
            it.type.isClassType(toJvmAsyncAnnotationName.toUnsafe())
                    || it.type.isClassType(toJvmBlockingAnnotationName.toUnsafe())
                    || it.type.isClassType(toJsPromiseAnnotationName.toUnsafe())
        }
    
    private object Generated : CallableDescriptor.UserDataKey<Boolean>
    
    private fun FunctionDescriptor.isGenerated(): Boolean {
        return this.annotations.hasAnnotation(generatedAnnotationName)
    }
    
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun IrFunction.isGenerated(): Boolean {
        return descriptor.isGenerated()
    }
    
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        resolveFunction(declaration)
        return super.visitFunctionNew(declaration)
    }
    
    
    private fun resolveFunction(
        function: IrFunction,
    ): IrFunction? {
        val parent = function.parent
        if (parent is IrClass) {
            parent.transformDeclarationsFlat { declaration ->
                buildList {
                    add(declaration)
                    
                    if (declaration is IrSimpleFunction) {
                        if (declaration.isGenerated()) {
                            return@transformDeclarationsFlat listOf()
                        }
                        
                        // suspend, with annotation
                        if (pluginContext.platform?.isJvm() == true && declaration.isSuspend) {
                            addAll(pluginContext.generateJvmFunction(declaration))
                        }
                    }
                }.also {
                    println("Generated size: ${it.size}")
                    if (it.size > 1) {
                        declaration.annotations +=
                            pluginContext.createIrBuilder(declaration.symbol)
                                .irAnnotationConstructor(generatedAnnotation)
                    }
                }
            }
        }
        
        return null
    }
    
    private fun IrPluginContext.generateJvmFunction(originFunction: IrFunction): List<IrDeclaration> {
        fun checkExists(suffix: String): Boolean {
            val functionName = "${originFunction.name}$suffix"
            // if function exists, skip.
            val parent = originFunction.parentClassOrNull ?: originFunction.fileParent
            return parent.functionsSequence.any { f ->
                if (f.name.asString() != functionName) {
                    return@any false
                }
                if (f.allParametersCount != originFunction.allParametersCount) {
                    return@any false
                }
                val originFunctionParameters = originFunction.allParameters
                f.allParameters.forEachIndexed { index, parameter ->
                    val targetOriginFunctionParameter = originFunctionParameters[index]
                    if (targetOriginFunctionParameter.type != parameter.type) {
                        return@any false
                    }
                }
                
                true
            }
        }
        
        return buildList {
            if (originFunction.hasAnnotation(toJvmBlockingAnnotationName) && !checkExists("Blocking")) {
                addAll(generateJvmBlockingFunction(originFunction))
            }
            if (originFunction.hasAnnotation(toJvmAsyncAnnotationName) && !checkExists("Async")) {
                addAll(generateJvmAsyncFunction(originFunction))
            }
        }
    }
    
    private fun IrPluginContext.generateJvmBlockingFunction(originFunction: IrFunction): List<IrDeclaration> {
        val blockingFunction = originFunction.copyFunc({
            name = Name.identifier("${originFunction.name}Blocking")
            returnType = originFunction.returnType
        }) bkf@{
            copyAttributes(originFunction as IrAttributeContainer)
            copyParameterDeclarationsFrom(originFunction)
            
            annotations = originFunction.annotations.filterNotCompileAnnotations()
                .plus(createIrBuilder(symbol).irAnnotationConstructor(generatedAnnotation))
            
            // @JvmDefault?
            // if (parentClassOrFile.isInterface) {
            //     this.annotations += createIrBuilder(symbol).irAnnotationConstructor(referenceJvmDefault())
            // }
            
            body = createIrBuilder(symbol).irBlockBody {
                val suspendLambda = createSuspendLambdaWithCoroutineScope(
                    parent = this@bkf,
                    // suspend () -> R
                    lambdaType = symbols.suspendFunctionN(0).typeWith(this@bkf.returnType),
                    originFunction = originFunction
                ).also { +it }
                
                +irReturn(irCall(jvmRunBlockingFunction).apply {
                    // 0: CoroutineContext
                    putValueArgument(1, irCall(suspendLambda.primaryConstructor!!).apply {
                        for ((index, parameter) in this@bkf.paramsAndReceiversAsParamsList().withIndex()) {
                            putValueArgument(index, irGet(parameter))
                        }
                    })
                })
            }
        }
        
        return listOf(blockingFunction)
    }
    
    private fun IrPluginContext.generateJvmAsyncFunction(originFunction: IrFunction): List<IrDeclaration> {
        val asyncFunction = originFunction.copyFunc({
            name = Name.identifier("${originFunction.name}Async")
            returnType = completableFutureClass.typeWith(originFunction.returnType)
        }) af@{
            copyAttributes(originFunction as IrAttributeContainer)
            copyParameterDeclarationsFrom(originFunction)
            
            annotations = originFunction.annotations.filterNotCompileAnnotations()
                .plus(createIrBuilder(symbol).irAnnotationConstructor(generatedAnnotation))
            
            body = createIrBuilder(symbol).irBlockBody {
                val suspendLambda = createSuspendLambdaWithCoroutineScope(
                    parent = this@af,
                    // suspend () -> R
                    lambdaType = symbols.suspendFunctionN(0).typeWith(this@af.returnType),
                    originFunction = originFunction
                ).also { +it }
                
                +irReturn(irCall(jvmRunAsyncFunction).apply {
                    // 0: CoroutineContext
                    putValueArgument(1, irCall(suspendLambda.primaryConstructor!!).apply {
                        for ((index, parameter) in this@af.paramsAndReceiversAsParamsList().withIndex()) {
                            putValueArgument(index, irGet(parameter))
                        }
                    })
                })
            }
            
            
        }
        
        return listOf(asyncFunction)
    }
    
    private fun IrPluginContext.generateJsPromiseFunction(originFunction: IrFunction): List<IrDeclaration> {
        
        TODO()
    }
}

internal fun IrBuilderWithScope.irAnnotationConstructor(
    clazz: IrClassSymbol,
): IrConstructorCall {
    return run {
        irCall(clazz.constructors.first())
    }.run {
        irConstructorCall(this, this.symbol)
    }
}

private inline fun IrFunction.copyFunc(
    builder: IrFunctionBuilder.() -> Unit = {},
    andThen: IrSimpleFunction.() -> Unit = {},
): IrFunction {
    val originFunction = this
    val parentClassOrFile = originFunction.parentClassOrNull ?: fileParent
    val copyFunction = IrFactoryImpl.buildFun {
        startOffset = originFunction.startOffset
        endOffset = originFunction.endOffset
        origin = IrDeclarationOrigin.DEFINED
        returnType = originalFunction.returnType
        modality = parentClassOrFile.computeModality()
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
    
    
    return copyFunction.apply(andThen)
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

private fun IrDeclarationContainer.computeModality(): Modality {
    if (isInterface) return Modality.OPEN
    return when {
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

private val IrDeclarationContainer.isPrivate: Boolean
    get() = (this as? IrDeclarationWithVisibility)?.visibility?.delegate == Visibilities.Private


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