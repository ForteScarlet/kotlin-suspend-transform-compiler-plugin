package love.forte.plugin.suspendtrans

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.*
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.fileParent
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
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
class SuspendTransformTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {
    private val toJvmBlockingAnnotationName =
        FqName("love.forte.plugin.suspendtrans.annotation.Suspend2JvmBlocking")
    
    private val toJvmBlockingAnnotation =
        pluginContext.referenceClass(toJvmBlockingAnnotationName)!!
    
    private val toJvmAsyncAnnotation =
        pluginContext.referenceClass(FqName("love.forte.plugin.suspendtrans.annotation.Suspend2JvmAsync"))!!
    
    private val toJsPromiseAnnotation =
        pluginContext.referenceClass(FqName("love.forte.plugin.suspendtrans.annotation.Suspend2JsPromise"))!!
    
    private val typeUnit = pluginContext.irBuiltIns.unitType
    
    private val runBlockingFunction =
        pluginContext.referenceFunctions(FqName("love.forte.plugin.suspendtrans.runInBlocking"))
            .single()
    
    private val classMonotonic =
        pluginContext.referenceClass(FqName("kotlin.time.TimeSource.Monotonic"))!!
    
    private val funMarkNow =
        pluginContext.referenceFunctions(FqName("kotlin.time.TimeSource.markNow"))
            .single()
    
    private val funElapsedNow =
        pluginContext.referenceFunctions(FqName("kotlin.time.TimeMark.elapsedNow"))
            .single()
    
    
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun visitFunctionNew(function: IrFunction): IrStatement {
        if (!function.isSuspend) {
            return super.visitFunctionNew(function)
        }
        
        val parent = function.parent
        
        
        when {
            pluginContext.platform?.isJvm() == true
                    && function.hasAnnotation(toJvmBlockingAnnotation) -> {
                
                // val annotation = function.getAnnotation(toJvmBlockingAnnotationName)
                //
                // val owner = annotation.symbol.owner
                // println(owner)
                // println(owner::class)
                
                irSuspendToJvmBlocking(function, ToJvmBlockingData(function.name.asString(), "Blocking"))
            }
            
        }
        
        return super.visitFunctionNew(function)
    }
    
    
    // override fun visitClassNew(declaration: IrClass): IrStatement {
    //     println("visit class: $declaration")
    //     return super.visitClassNew(declaration)
    // }
    
    // override fun visitFunctionNew(declaration: IrFunction): IrStatement {
    //     println("visit function: $declaration")
    //     return super.visitFunctionNew(declaration)
    // }
    
    // override fun visitFunctionNew(declaration: IrFunction): IrStatement {
    //     println("visit function: $declaration")
    //     return super.visitFunctionNew(declaration)
    // }
    
    // override fun visitFunctionNew(declaration: IrFunction): IrStatement {
    //     val body = declaration.body
    //     if (body != null && declaration.hasAnnotation(toJvmBlockingAnnotation)) {
    //         declaration.body = irSuspendToJvmBlocking(declaration, body)
    //     }
    //     return super.visitFunctionNew(declaration)
    // }
    
    private data class ToJvmBlockingData(val baseName: String, val suffix: String) {
        val functionName = "$baseName$suffix"
    }
    
    
    private fun irSuspendToJvmBlocking(
        function: IrFunction,
        data: ToJvmBlockingData,
    ): IrFunction? {
        val parent = function.parent
        if (parent is IrClass) {
            parent.transformDeclarationsFlat { declaration ->
                var result = listOf(declaration)
                if (declaration is IrSimpleFunction) {
                    
                    // suspend, with annotation
                    if (pluginContext.platform?.isJvm() == true
                        && declaration.isSuspend
                        && declaration.hasAnnotation(toJvmBlockingAnnotation)
                    ) {
                        val blockingFunction = pluginContext.generateJvmBlockingFunction(declaration)
                        result = result + blockingFunction
                    }
                    
                }
                
                result
            }
            
            
            // return parent.addFunction {
            //     updateFrom(function)
            //     name = Name.identifier(data.functionName)
            //     isSuspend = false
            //     returnType = function.returnType
            //
            // }.apply {
            //     // not static
            //     function.dispatchReceiverParameter?.also { dispatchReceiverParameter ->
            //         this.dispatchReceiverParameter = dispatchReceiverParameter.copyTo(this)
            //     }
            //     function.extensionReceiverParameter?.also { extensionReceiverParameter ->
            //         this.extensionReceiverParameter = extensionReceiverParameter.copyTo(this)
            //     }
            //
            //     this.body = DeclarationIrBuilder(pluginContext, this.symbol).irBlockBody {
            //         val blockingCall = irJvmBlockingCall(function)
            //
            //         +irReturn(blockingCall)
            //     }
            // }
        }
        
        return null
    }
    
    private fun IrPluginContext.generateJvmBlockingFunction(originFunction: IrFunction): List<IrDeclaration> {
        val parentClassOrFile: IrDeclarationContainer = originFunction.parentClassOrNull ?: originFunction.fileParent
        
        val blockingFunction = IrFactoryImpl.buildFun {
            startOffset = originFunction.startOffset
            endOffset = originFunction.endOffset
            
            origin = IrDeclarationOrigin.DEFINED
            
            name = Name.identifier("${originFunction.name}Blocking")
            
            returnType = originFunction.returnType
            
            modality = parentClassOrFile.computeModality()
            
            visibility =
                if (parentClassOrFile.isInterface) DescriptorVisibilities.PUBLIC
                else originFunction.visibility
            
            isSuspend = false
            isExternal = false
            isExpect = false
        }
        
        blockingFunction.apply bkf@{
            copyAttributes(originFunction as IrAttributeContainer)
            copyParameterDeclarationsFrom(originFunction)
            
            this.annotations = originFunction.annotations
                .filterNot { it.type.isClassType(toJvmBlockingAnnotationName.toUnsafe()) }
            // TODO generated annotation
            //.plus(createGeneratedBlockingBridgeConstructorCall(symbol))
            
            // @JvmDefault?
            // if (parentClassOrFile.isInterface) {
            //     this.annotations += createIrBuilder(symbol).irAnnotationConstructor(referenceJvmDefault())
            // }
            
            parent = parentClassOrFile
            extensionReceiverParameter = originFunction.extensionReceiverParameter?.copyTo(this)
            dispatchReceiverParameter =
                if (originFunction.isStatic) null
                else originFunction.dispatchReceiverParameter?.copyTo(this)
            
            body = createIrBuilder(symbol).irBlockBody {
                
                val suspendLambda = createSuspendLambdaWithCoroutineScope(
                    parent = this@bkf,
                    lambdaType = symbols.suspendFunctionN(0).typeWith(this@bkf.returnType), // suspend () -> R
                    originFunction = originFunction
                ).also { +it }
                
                +irReturn(
                    irCall(runBlockingFunction).apply {
                        // putTypeArgument(0, this@fn.returnType) // the R for runBlocking
                        
                        // take default value for value argument 0
                        
                        putValueArgument(1, irCall(suspendLambda.primaryConstructor!!).apply {
                            for ((index, parameter) in this@bkf.paramsAndReceiversAsParamsList().withIndex()) {
                                putValueArgument(index, irGet(parameter))
                            }
                        })
                    }
                )
                
            }
            
        }
        
        
        return listOf(blockingFunction)
    }
    
    
    private fun IrBuilderWithScope.irJvmBlockingCall(
        function: IrFunction,
    ): IrCall {
        
        val runBlockingBlock = irBlock(resultType = function.returnType) {
            // runBlocking { func(//) }
            // function.valueParameters
            // +irReturn(irCall(function.symbol).also { call ->
            //
            // })
        }
        
        return irCall(runBlockingFunction).also { call ->
            call.putValueArgument(1, irCall(function))
        }
    }
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