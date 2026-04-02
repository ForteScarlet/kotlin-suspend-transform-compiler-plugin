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
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.processOverriddenFunctionsSafe
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.resolve.getSuperTypes
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.name.Name

/**
 * Determines whether a generated declaration should be marked as `override`
 * based on the source function's override chain and matching transformation metadata.
 */
internal fun SuspendTransformFirTransformer.checkSyntheticFunctionIsOverrideBasedOnSourceFunction(
    syntheticFunData: SyntheticFunData,
    func: FirNamedFunctionSymbol,
    checkContext: CheckerContext
): Boolean {
    // Check the overridden for isOverride based on source function (func) 's overridden
    var isOverride = false
    val annoData = syntheticFunData.annotationData
    val markAnnotation = syntheticFunData.transformer.markAnnotation

    if (func.isOverride && !isOverride) {
        with(checkContext) {
            func.processOverriddenFunctionsSafe processOverridden@{ overriddenFunction ->
                if (!isOverride) {
                    // check parameters and receivers
                    val resolvedReceiverTypeRef = overriddenFunction.resolvedReceiverTypeRef
                    val originReceiverTypeRef = func.resolvedReceiverTypeRef

                    // origin receiver should be the same as symbol receiver
                    if (originReceiverTypeRef != resolvedReceiverTypeRef) {
                        return@processOverridden
                    }

                    // all value parameters should be a subtype of symbol's value parameters
                    val symbolParameterSymbols = overriddenFunction.valueParameterSymbols
                    val originParameterSymbols = func.valueParameterSymbols

                    if (symbolParameterSymbols.size != originParameterSymbols.size) {
                        return@processOverridden
                    }

                    for ((index, symbolParameter) in symbolParameterSymbols.withIndex()) {
                        val originParameter = originParameterSymbols[index]
                        if (
                            originParameter.resolvedReturnType != symbolParameter.resolvedReturnType
                        ) {
                            return@processOverridden
                        }
                    }

                    val overriddenAnnotation = firAnnotation(
                        overriddenFunction, markAnnotation, overriddenFunction.getContainingClassSymbol()
                    ) ?: return@processOverridden

                    val overriddenAnnoData = runTransformAnnotationData(
                        overriddenAnnotation,
                        markAnnotation,
                        overriddenFunction.name.asString()
                    )

                    // Same functionName, same asProperty, the generated synthetic function will be same too.
                    if (
                        overriddenAnnoData.functionName == annoData.functionName
                        && overriddenAnnoData.asProperty == annoData.asProperty
                    ) {
                        isOverride = true
                    }
                }
            }
        }
    }
    return isOverride
}

/**
 * Checks whether a generated member signature can override a compatible member from a super type.
 */
@OptIn(DirectDeclarationsAccess::class)
internal fun isOverridable(
    session: FirSession,
    functionName: Name,
    thisReceiverTypeRef: org.jetbrains.kotlin.fir.types.FirTypeRef?,
    thisValueTypeRefs: List<org.jetbrains.kotlin.fir.types.FirTypeRef?>,
    owner: FirClassSymbol<*>,
    isProperty: Boolean,
): Boolean {
    if (isProperty) {
        // value symbols must be empty.
        check(thisValueTypeRefs.isEmpty()) { "property's value parameters must be empty." }

        return owner.getSuperTypes(session)
            .asSequence()
            .mapNotNull { it.toRegularClassSymbol(session) }
            .flatMap {
                it.declarationSymbols.filterIsInstance<FirPropertySymbol>()
            }
            .filter { !it.isFinal }
            .filter { it.callableId?.callableName == functionName }
            // overridable receiver parameter.
            .filter { thisReceiverTypeRef sameAs it.resolvedReceiverTypeRef }
            .any()
    } else {
        return owner.getSuperTypes(session)
            .asSequence()
            .mapNotNull { it.toRegularClassSymbol(session) }
            .flatMap {
                it.declarationSymbols.filterIsInstance<FirNamedFunctionSymbol>()
            }
            // not final, overridable
            .filter { !it.isFinal }
            // same name
            .filter { it.callableId.callableName == functionName }
            // overridable receiver parameter.
            .filter { thisReceiverTypeRef sameAs it.resolvedReceiverTypeRef }
            // overridable value parameters
            .filter {
                val valuePs = it.valueParameterSymbols
                    .map { vps -> vps.resolvedReturnTypeRef }

                if (valuePs.size != thisValueTypeRefs.size) return@filter false

                for (i in valuePs.indices) {
                    val valueP = valuePs[i]
                    val thisP = thisValueTypeRefs[i]

                    if (thisP notSameAs valueP) {
                        return@filter false
                    }
                }

                true
            }
            .any()
    }
}

internal fun isOverridable(
    session: FirSession,
    functionName: Name,
    originFunc: FirNamedFunction,
    owner: FirClassSymbol<*>,
    isProperty: Boolean = false,
): Boolean {
    // 寻找 owner 中所有的 open/abstract的,
    // parameters 的类型跟 originFunc 的 parameters 匹配的

    val thisReceiverTypeRef = originFunc.receiverParameter?.typeRef

    val thisValueTypeRefs = originFunc.valueParameters.map {
        it.symbol.resolvedReturnTypeRef
    }

    return isOverridable(session, functionName, thisReceiverTypeRef, thisValueTypeRefs, owner, isProperty)
}

/**
 * Checks whether two FIR type references represent the same override-relevant type.
 */
internal infix fun org.jetbrains.kotlin.fir.types.FirTypeRef?.sameAs(otherSuper: org.jetbrains.kotlin.fir.types.FirTypeRef?): Boolean {
    if (this == otherSuper) return true
    val thisConeType = this?.coneTypeOrNull
    val otherConeType = otherSuper?.coneTypeOrNull

    if (thisConeType == otherConeType) return true

    if (thisConeType == null || otherConeType == null) {
        // One this null, other is not null
        return false
    }

    return thisConeType sameAs otherConeType
}

internal infix fun ConeKotlinType.sameAs(otherSuper: ConeKotlinType): Boolean {
    return this == otherSuper
    // 有什么便捷的方法来处理 ConeTypeParameterType ？
}

internal infix fun org.jetbrains.kotlin.fir.types.FirTypeRef?.notSameAs(otherSuper: org.jetbrains.kotlin.fir.types.FirTypeRef?): Boolean =
    !(this sameAs otherSuper)
