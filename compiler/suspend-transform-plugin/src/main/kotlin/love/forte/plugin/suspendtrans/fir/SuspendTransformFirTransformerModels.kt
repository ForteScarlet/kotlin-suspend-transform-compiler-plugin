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

import love.forte.plugin.suspendtrans.configuration.MarkAnnotation
import love.forte.plugin.suspendtrans.configuration.Transformer
import love.forte.plugin.suspendtrans.fqn
import love.forte.plugin.suspendtrans.utils.TransformAnnotationData
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.scopes.impl.FirClassDeclaredMemberScope
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Tracks the relation between an original type parameter and its copied counterpart
 * while synthetic FIR declarations are rebuilt.
 */
internal data class CopiedTypeParameterPair(
    val original: FirTypeParameter,
    val copied: FirTypeParameter
)

/**
 * Groups annotations copied to generated declarations and annotations added back
 * to the original declaration.
 */
internal data class CopyAnnotations(
    val functionAnnotations: List<FirAnnotation>,
    val propertyAnnotations: List<FirAnnotation>,
    val toOriginalAnnotations: List<FirAnnotation>
)

/**
 * Cache key for synthetic-member lookup in a specific class and declared scope.
 */
internal data class FirCacheKey(
    val classSymbol: FirClassSymbol<*>,
    val memberScope: FirClassDeclaredMemberScope?
)

/**
 * Describes a synthetic function or property accessor that should be generated
 * for a suspend source declaration.
 */
internal data class SyntheticFunData(
    val funName: Name,
    val annotationData: TransformAnnotationData,
    val transformer: Transformer,
    val transformerFunctionSymbol: FirNamedFunctionSymbol,
)

/**
 * Resolves the marker annotation into a FIR [ClassId].
 */
internal val MarkAnnotation.classId: ClassId
    get() = ClassId(classInfo.packageName.fqn, classInfo.className.fqn, classInfo.local)

/**
 * Resolves the marker annotation into an [FqName] for FIR predicate registration.
 */
internal val MarkAnnotation.fqName: FqName
    get() = FqName(classInfo.packageName + "." + classInfo.className)

/**
 * Normalizes the modality used by generated non-suspend members so that they can
 * still participate in overriding when the original declaration did.
 */
internal val FirNamedFunction.syntheticModifier: Modality?
    get() = when {
        status.isOverride -> Modality.OPEN
        modality == Modality.ABSTRACT -> Modality.OPEN
        else -> status.modality
    }
