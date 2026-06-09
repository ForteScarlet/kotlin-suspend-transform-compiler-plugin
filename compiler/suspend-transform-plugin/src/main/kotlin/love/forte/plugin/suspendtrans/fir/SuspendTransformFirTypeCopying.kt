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

package love.forte.plugin.suspendtrans.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirReceiverParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.fir.types.withReplacedConeType

/**
 * Copies function type parameters and rebinds them to the generated declaration symbol.
 */
internal fun List<FirTypeParameter>.mapToNewTypeParameters(
    funSymbol: FirBasedSymbol<*>,
    originalTypeParameterCache: MutableList<CopiedTypeParameterPair>
): List<FirTypeParameter> {
    return map {
        buildTypeParameterCopy(it) {
            containingDeclarationSymbol = funSymbol
//                             symbol = it.symbol // FirTypeParameterSymbol()
            symbol = FirTypeParameterSymbol()
        }.also { new ->
            originalTypeParameterCache.add(CopiedTypeParameterPair(it, new))
        }
    }
}

/**
 * Copies value parameters and rewrites their types to reference copied type parameters.
 */
internal fun List<FirValueParameter>.mapToNewValueParameters(
    originalTypeParameterCache: MutableList<CopiedTypeParameterPair>,
    newContainingDeclarationSymbol: FirBasedSymbol<*>,
    session: FirSession
): List<FirValueParameter> {
    return map { vp ->
        buildValueParameterCopy(vp) {
            symbol = FirValueParameterSymbol()
            containingDeclarationSymbol = newContainingDeclarationSymbol

            val copiedConeType = vp.returnTypeRef.coneTypeOrNull
                ?.copyWithTypeParameters(originalTypeParameterCache, session)

            if (copiedConeType != null) {
                returnTypeRef = returnTypeRef.withReplacedConeType(copiedConeType)
            }
        }
    }
}

/**
 * Copies an extension receiver and remaps any function-scoped type parameters it uses.
 */
internal fun FirReceiverParameter.copyToNew(
    originalTypeParameterCache: MutableList<CopiedTypeParameterPair>,
    newContainingDeclarationSymbol: FirBasedSymbol<*>,
    session: FirSession
): FirReceiverParameter? {
    return typeRef.coneTypeOrNull
        ?.copyWithTypeParameters(originalTypeParameterCache, session)
        ?.let { foundCopied ->
            buildReceiverParameterCopy(this) {
                symbol = FirReceiverParameterSymbol()
                containingDeclarationSymbol = newContainingDeclarationSymbol
                typeRef = typeRef.withReplacedConeType(foundCopied)
            }
        }
}

/**
 * Rebuilds copied function parameters so the generated declaration owns its symbols
 * and any function-scoped generic parameters.
 */
internal fun FirNamedFunctionBuilder.copyParameters(session: FirSession) {
    val newFunSymbol = symbol
    val originalTypeParameterCache = mutableListOf<CopiedTypeParameterPair>()

    val newTypeParameters = typeParameters.mapToNewTypeParameters(newFunSymbol, originalTypeParameterCache)
    typeParameters.clear()
    typeParameters.addAll(newTypeParameters)

    val newContextParameters = contextParameters.mapToNewValueParameters(
        originalTypeParameterCache,
        newFunSymbol,
        session,
    )
    contextParameters.clear()
    contextParameters.addAll(newContextParameters)

    val newValueParameters = valueParameters.mapToNewValueParameters(
        originalTypeParameterCache,
        newFunSymbol,
        session
    )
    valueParameters.clear()
    valueParameters.addAll(newValueParameters)

    receiverParameter?.copyToNew(originalTypeParameterCache, newFunSymbol, session)?.also {
        this.receiverParameter = it
    }

    val coneTypeOrNull = returnTypeRef.coneTypeOrNull
    if (coneTypeOrNull != null) {
        returnTypeRef = returnTypeRef
            .withReplacedConeType(coneTypeOrNull.copyConeTypeOrSelf(originalTypeParameterCache, session))
    }
}

/**
 * Rebuilds accessor value parameters and optionally its return type.
 */
internal fun FirPropertyAccessorBuilder.copyParameters(
    newFunSymbol: FirBasedSymbol<*>,
    session: FirSession,
    originalTypeParameterCache: MutableList<CopiedTypeParameterPair> = mutableListOf(),
    copyReturnType: Boolean = true,
) {
    // 的确，property 哪儿来的 type parameter
//        val newTypeParameters = typeParameters.mapToNewTypeParameters(symbol, originalTypeParameterCache)
//        typeParameters.clear()
//        typeParameters.addAll(newTypeParameters)

    val newValueParameters = valueParameters.mapToNewValueParameters(
        originalTypeParameterCache,
        newFunSymbol,
        session
    )
    valueParameters.clear()
    valueParameters.addAll(newValueParameters)

    if (copyReturnType) {
        val coneTypeOrNull = returnTypeRef.coneTypeOrNull
        if (coneTypeOrNull != null) {
            returnTypeRef = returnTypeRef
                .withReplacedConeType(coneTypeOrNull.copyConeTypeOrSelf(originalTypeParameterCache, session))
        }
    }
}
