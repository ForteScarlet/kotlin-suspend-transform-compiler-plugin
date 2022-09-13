package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.fileParent
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import java.util.*

fun IrSimpleFunction.asProperty(): IrProperty {
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


/**
 * Generates an anonymous object.
 *
 * - extends `suspend () -> Unit`.
 * - takes dispatch and extension receivers as param, followed by normal value params, to the constructor of this object
 */
fun IrPluginContext.createSuspendLambdaWithCoroutineScope(
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

fun IrFunction.paramsAndReceiversAsParamsList(): List<IrValueParameter> {
    return buildList {
        if (!isStatic) {
            dispatchReceiverParameter?.let(this::add)
        }
        extensionReceiverParameter?.let(this::add)
        valueParameters.let(this::addAll)
    }
}


val Name.identifierOrMappedSpecialName: String
    get() {
        return when (this.asString()) {
            "<this>" -> "\$receiver" // finally synthesized as
            else -> this.identifier
        }
    }


val IrDeclarationContainer.functionsSequence: Sequence<IrSimpleFunction>
    get() = declarations.asSequence().filterIsInstance<IrSimpleFunction>()