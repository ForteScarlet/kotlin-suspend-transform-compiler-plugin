/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package love.forte.plugin.suspendtrans.ir

import love.forte.plugin.suspendtrans.SuspendTransformUserDataKey
import love.forte.plugin.suspendtrans.checkSame
import love.forte.plugin.suspendtrans.configuration.Transformer
import love.forte.plugin.suspendtrans.fir.SuspendTransformBridgeFunctionKey
import love.forte.plugin.suspendtrans.fir.SuspendTransformGeneratedDeclarationKey
import love.forte.plugin.suspendtrans.fir.SuspendTransformK2V3Key
import love.forte.plugin.suspendtrans.fir.SuspendTransformPluginKey
import love.forte.plugin.suspendtrans.utils.toCallableId
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

/**
 * Resolves the transformer bridge function declared by plugin configuration.
 */
internal fun SuspendTransformTransformer.findTransformTargetFunction(transformer: Transformer): IrSimpleFunctionSymbol {
    return pluginContext
        .referenceFunctions(transformer.transformFunctionInfo.toCallableId())
        .firstOrNull()
        ?: throw IllegalStateException("Transform function ${transformer.transformFunctionInfo} not found")
}

/**
 * Resolves bodies for plugin-generated IR declarations by locating their source suspend declaration
 * or bridge target and delegating to the shared body builder.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
internal fun SuspendTransformTransformer.resolveFunctionBodyByDescriptor(
    declaration: IrFunction,
    descriptor: CallableDescriptor,
    property: IrProperty? = null
): IrFunction? {
    // K2
    val pluginKey = resolveGeneratedDeclarationKey(declaration, property)

    // K1 ?
    val userData = descriptor.getUserData(SuspendTransformUserDataKey)

    return when {
        // K2 v3: body is already generated in FIR, so skip it here.
        declaration.body != null -> null

        pluginKey is SuspendTransformK2V3Key -> null

        // K2 v2
        pluginKey is SuspendTransformBridgeFunctionKey -> {
            // TODO Maybe `.finderForSource(..)` could support same-module lookups here.
            resolveBridgeFunctionBody(
                pluginKey,
                declaration,
                findTransformTargetFunction(pluginKey.data.transformer)
            )

            null
//                        .also { generatedOriginFunction ->
//                        if (property != null) {
//                            // NO! BACKING! FIELD!
//                            property.backingField = null
//                        }
//
//                        if (generatedOriginFunction != null) {
//                            postProcessGenerateOriginFunction(
//                                generatedOriginFunction,
//                                pluginKey.data.transformer.originFunctionIncludeAnnotations
//                            )
//                        }
//                    }
        }

        pluginKey is SuspendTransformPluginKey -> {
            resolveFunctionBody(
                pluginKey,
                declaration,
                { f ->
                    pluginKey.data.originSymbol.checkSame(pluginKey.data.markerId, f)
                },
                findTransformTargetFunction(pluginKey.data.transformer)
            ).also { generatedOriginFunction ->
                clearSyntheticPropertyBackingField(property)

                if (generatedOriginFunction != null) {
                    postProcessGenerateOriginFunction(
                        generatedOriginFunction,
                        pluginKey.data.transformer.originFunctionIncludeAnnotations
                    )
                }
            }
        }

        userData != null -> {
            resolveFunctionBody(
                userData,
                declaration,
//                { f -> userData.originFunctionSymbol.isSame(f).also { println("IsSame: ${userData.originFunctionSymbol} -> $f") } },
                { f -> f.descriptor == userData.originFunction },
                findTransformTargetFunction(userData.transformer)
            )?.also { generatedOriginFunction ->
                postProcessGenerateOriginFunction(
                    generatedOriginFunction,
                    userData.transformer.originFunctionIncludeAnnotations
                )
            }
        }

        else -> null
    }
}

/**
 * Extracts the generated declaration key from either the property wrapper or the
 * generated function itself.
 */
private fun resolveGeneratedDeclarationKey(
    declaration: IrFunction,
    property: IrProperty?,
): SuspendTransformGeneratedDeclarationKey? {
    return if (property != null) {
        // from property
        (property.origin as? IrDeclarationOrigin.GeneratedByPlugin)
            ?.pluginKey as? SuspendTransformGeneratedDeclarationKey
    } else {
        (declaration.origin as? IrDeclarationOrigin.GeneratedByPlugin)
            ?.pluginKey as? SuspendTransformGeneratedDeclarationKey
    }
}

/**
 * Clears the synthetic property's backing field because the generated property is getter-only.
 */
private fun clearSyntheticPropertyBackingField(property: IrProperty?) {
    if (property != null) {
        // NO! BACKING! FIELD!
        property.backingField = null
    }
}
