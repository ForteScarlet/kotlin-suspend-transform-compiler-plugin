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
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.types.*

/**
 * Attempts to copy a FIR type while remapping any function-scoped type parameters.
 */
internal fun ConeKotlinType.copyConeType(
    originalTypeParameterCache: MutableList<CopiedTypeParameterPair>,
    session: FirSession
): ConeKotlinType? {
    return copyWithTypeParameters(originalTypeParameterCache, session)
}

/**
 * Copies a FIR type while preserving the original value when no remapping is needed.
 */
internal fun ConeKotlinType.copyConeTypeOrSelf(
    originalTypeParameterCache: MutableList<CopiedTypeParameterPair>,
    session: FirSession
): ConeKotlinType {
    return copyConeType(originalTypeParameterCache, session) ?: this
}

/**
 * Copies a FIR type while remapping any references to copied function-scoped type
 * parameters. The implementation intentionally preserves the original unsupported
 * branches and comments because it mirrors the existing behavior.
 */
internal fun ConeKotlinType.copyWithTypeParameters(
    parameters: List<CopiedTypeParameterPair>,
    session: FirSession,
): ConeKotlinType? {
    fun findCopied(target: ConeKotlinType) = parameters.find { (original, _) ->
        original.symbol.toConeType() == target
    }?.copied

    val copiedThis = findCopied(this)
    if (copiedThis != null) {
        return copiedThis.symbol.toConeType()
    }

    when (this) {
        is ConeDynamicType -> {
        }

        is ConeFlexibleType -> {
        }

        is ConeClassLikeType -> {
            if (typeArguments.isNotEmpty()) {
                fun mapProjection(projection: ConeTypeProjection): ConeTypeProjection? {
                    val findCopiedDirectly = projection.type?.let { type -> findCopied(type) }
                    if (findCopiedDirectly != null) {
                        return findCopiedDirectly.symbol.toConeType()
                    }

                    return when (projection) {
                        // is ConeFlexibleType -> { }

                        is ConeClassLikeType -> {
                            projection.copyWithTypeParameters(parameters, session)
                        }

                        is ConeCapturedType -> {
                            val constructorLowerType = projection.constructor.lowerType?.copyWithTypeParameters(parameters, session)

                            if (constructorLowerType == null) {
                                null
                            } else {
                                projection.copy(
                                    constructor = ConeCapturedTypeConstructor(
                                        projection = projection.constructor.projection,
                                        lowerType = constructorLowerType,
                                        captureStatus = projection.constructor.captureStatus,
                                        supertypes = projection.constructor.supertypes,
                                        typeParameterMarker = projection.constructor.typeParameterMarker,
                                    )
                                )
                            }
                        }

                        is ConeDefinitelyNotNullType -> {
                            findCopied(projection.original)
                                ?.symbol?.toConeType()
                                ?.let { projection.copy(it) }
                        }
                        // is ConeIntegerConstantOperatorType -> TODO()
                        // is ConeIntegerLiteralConstantType -> TODO()
                        is ConeIntersectionType -> {
                            val upperBoundForApproximation = projection.upperBoundForApproximation
                                ?.copyWithTypeParameters(parameters, session)
//                            val upperBoundForApproximation =
//                                projection.upperBoundForApproximation
//                                    ?.let { findCopied(it) }
//                                    ?.toConeType()

                            var anyIntersectedTypes = false

                            val intersectedTypes = projection.intersectedTypes.map { ktype ->
                                findCopied(ktype)?.symbol?.toConeType()
//                                ktype.copyWithTypeParameters(parameters, session)
                                    ?.also { anyIntersectedTypes = true }
                                    ?: ktype
                            }

                            if (upperBoundForApproximation != null || anyIntersectedTypes) {
                                ConeIntersectionType(
                                    intersectedTypes,
                                    upperBoundForApproximation
                                )
                            } else {
                                null
                            }
                        }
                        // is ConeLookupTagBasedType -> TODO()
                        // is ConeStubTypeForTypeVariableInSubtyping -> TODO()
                        // is ConeTypeVariableType -> TODO()
                        is ConeKotlinTypeConflictingProjection -> {
//                            findCopied(projection.type)
//                                ?.toConeType()
//                                ?.let { projection.copy(it) }

                            projection.type.copyWithTypeParameters(parameters, session)
                                ?.let { projection.copy(it) }
                        }

                        is ConeKotlinTypeProjectionIn -> {
//                            findCopied(projection.type)
//                                ?.toConeType()
//                                ?.let { projection.copy(it) }

                            projection.type.copyWithTypeParameters(parameters, session)
                                ?.let { projection.copy(it) }
                        }

                        is ConeKotlinTypeProjectionOut -> {
//                            findCopied(projection.type)
//                                ?.toConeType()
//                                ?.let { projection.copy(it) }

                            projection.type.copyWithTypeParameters(parameters, session)
                                ?.let { projection.copy(it) }
                        }

                        is ConeTypeParameterType -> {
//                            findCopied(projection)?.toConeType()
                            projection.copyWithTypeParameters(parameters, session)
                        }

                        ConeStarProjection -> ConeStarProjection

                        // Other unknowns, e.g., ClassLike
                        else -> null
                    }
                }

                val typeArguments: Array<ConeTypeProjection> = typeArguments.map { projection ->
                    mapProjection(projection) ?: projection
                }.toTypedArray()

                return classId.createConeType(
                    session = session,
                    typeArguments = typeArguments,
                    nullable = isMarkedNullable
                )
            }

            return classId.createConeType(session = session, nullable = isMarkedNullable)
        }

        is ConeTypeParameterType -> {
            return findCopied(this)?.symbol?.toConeType() ?: this
//            return parameters.find { (original, _) ->
//                original.symbol.toConeType() == this
//            }?.copied?.symbol?.toConeType() ?: this
        }

        else -> {
            // ?
        }
    }

    return null
}
