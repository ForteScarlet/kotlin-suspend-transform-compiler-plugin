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

import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildTypeOperatorCall
import org.jetbrains.kotlin.fir.references.builder.buildImplicitThisReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.getSuperTypes
import org.jetbrains.kotlin.fir.resolve.isSubtypeOf
import org.jetbrains.kotlin.fir.resolve.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.constructClassType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.fir.types.makeConeTypeDefinitelyNotNullOrNotNull
import org.jetbrains.kotlin.fir.types.typeContext

/**
 * Builds the bridge-function invocation that wraps the suspend lambda and any
 * additional implicit arguments required by the transformer signature.
 */
@OptIn(SymbolInternals::class)
internal fun SuspendTransformFirTransformer.buildBridgeFunctionCall(
    originFunSymbol: FirNamedFunctionSymbol,
    owner: FirClassSymbol<*>,
    bridgeFunSymbol: FirNamedFunctionSymbol,
    lambda: FirAnonymousFunction,
    returnType: ConeKotlinType?,
) = buildFunctionCall {
    coneTypeOrNull = returnType
    source = null
    calleeReference = buildResolvedNamedReference {
        source = null
        name = bridgeFunSymbol.name
        resolvedSymbol = bridgeFunSymbol
    }

    // this.dispatchReceiver = buildThisReceiverExpression {
    //     coneTypeOrNull = originFunSymbol.dispatchReceiverType
    //     source = originFunSymbol.source
    //     calleeReference = buildImplicitThisReference {
    //         boundSymbol = owner
    //     }
    // }

    argumentList = buildResolvedArgumentList(
        null,
        mapping = buildBridgeArgumentMapping(originFunSymbol, owner, bridgeFunSymbol, buildLambdaExpression(lambda))
    )
}

/**
 * Computes the bridge call arguments, including the generated suspend lambda and
 * any implicit `this` / `CoroutineScope` values.
 */
@OptIn(SymbolInternals::class)
private fun SuspendTransformFirTransformer.buildBridgeArgumentMapping(
    originFunSymbol: FirNamedFunctionSymbol,
    owner: FirClassSymbol<*>,
    bridgeFunSymbol: FirNamedFunctionSymbol,
    lambdaExpression: FirExpression,
): LinkedHashMap<FirExpression, FirValueParameter> {
    return linkedMapOf<FirExpression, FirValueParameter>().apply {
        put(
            lambdaExpression,
            //                                            funData.bridgeFunData.lambdaParameter
            bridgeFunSymbol.valueParameterSymbols.first().fir
        )

        // scope, if exists
        val valueParameterSymbols = bridgeFunSymbol.valueParameterSymbols

        if (valueParameterSymbols.size > 1) {
            // 支持:
            //  CoroutineScope? -> this as? CoroutineScope
            //  CoroutineScope -> this or throw error
            //  CoroutineScope (optional) -> this or ignore
            //  Any -> this
            //  index 1 以及后面的所有参数都进行处理

            fun ConeKotlinType.isCoroutineScope(): Boolean {
                return isSubtypeOf(
                    coroutineScopeSymbol.toLookupTag().constructClassType(),
                    firSession
                )
            }

            val listIterator = valueParameterSymbols.listIterator(1)
            listIterator.forEach { parameterSymbol ->
                val parameterFir = parameterSymbol.fir
                val parameterType = parameterSymbol.resolvedReturnType

                val parameterTypeNotNullable = if (parameterType.isMarkedNullable) {
                    parameterType.makeConeTypeDefinitelyNotNullOrNotNull(firSession.typeContext)
                } else {
                    parameterType
                }

                when {
                    // 参数是 CoroutineScope(?) 类型
                    parameterTypeNotNullable.isCoroutineScope() -> {
                        if (parameterType.isMarkedNullable) {
                            // scope = this as? CoroutineScope
                            put(
                                buildTypeOperatorCall {
                                    source = null
                                    coneTypeOrNull = parameterTypeNotNullable
                                    argumentList = buildResolvedArgumentList(
                                        null,
                                        mapping = linkedMapOf<FirExpression, FirValueParameter>().apply {
                                            put(buildOwnerThisReceiverExpression(originFunSymbol, owner), parameterFir)
                                        }
                                    )
                                    operation = org.jetbrains.kotlin.fir.expressions.FirOperation.SAFE_AS
                                    conversionTypeRef = parameterTypeNotNullable.toFirResolvedTypeRef()
                                },
                                parameterFir
                            )
                        } else {
                            // coroutine not nullable
                            // put if this is `CoroutineScope` or it is optional, otherwise throw error
                            var ownerIsCoroutineScopeOrParameterIsOptional =
                                parameterSymbol.hasDefaultValue
                            for (superType in owner.getSuperTypes(firSession, recursive = false)) {
                                if (superType.isCoroutineScope()) {
                                    put(buildOwnerThisReceiverExpression(originFunSymbol, owner), parameterFir)
                                    ownerIsCoroutineScopeOrParameterIsOptional = true
                                    break
                                }
                            }

                            // or throw error?
                            if (!ownerIsCoroutineScopeOrParameterIsOptional) {
                                error(
                                    "Owner is not a CoroutineScope, " +
                                        "and the transformer function requires a `CoroutineScope` parameter."
                                )
                            }
                        }
                    }

                    // 参数是 Any(?) 类型
                    parameterTypeNotNullable == firSession.builtinTypes.anyType.coneType -> {
                        // 直接把 this 放进去，不需要转换
                        put(buildOwnerThisReceiverExpression(originFunSymbol, owner), parameterFir)
                    }
                }
            }
        }
    }
}

/**
 * Creates a reusable anonymous-function expression node for the generated suspend lambda.
 */
private fun buildLambdaExpression(lambda: FirAnonymousFunction): FirExpression {
    return buildAnonymousFunctionExpression {
        source = null
        anonymousFunction = lambda
        isTrailingLambda = false
    }
}

/**
 * Creates the implicit `this` receiver used for bridge arguments and original-call dispatch.
 */
@OptIn(SymbolInternals::class)
internal fun buildOwnerThisReceiverExpression(
    originFunSymbol: FirNamedFunctionSymbol,
    owner: FirClassSymbol<*>,
): FirThisReceiverExpression {
    return buildThisReceiverExpression {
        coneTypeOrNull = originFunSymbol.dispatchReceiverType
        source = null
        calleeReference = buildImplicitThisReference {
            boundSymbol = owner
        }
    }
}
