package love.forte.plugin.suspendtrans.utils

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirEmptyArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCall
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

private val jsExportIgnore = ClassId.fromString("kotlin/js/JsExport.Ignore")
private val jvmSynthetic = ClassId.fromString("kotlin/jvm/JvmSynthetic")

fun FirDeclaration.excludeFromJsExport(session: FirSession) {
    if (!session.moduleData.platform.isJs()) {
        return
    }
    val jsExportIgnore = session.symbolProvider.getClassLikeSymbolByClassId(jsExportIgnore)
    val jsExportIgnoreAnnotation = jsExportIgnore as? FirRegularClassSymbol ?: return
    val jsExportIgnoreConstructor =
        jsExportIgnoreAnnotation.declarationSymbols.firstIsInstanceOrNull<FirConstructorSymbol>() ?: return

    val jsExportIgnoreAnnotationCall = buildAnnotationCall {
        argumentList = FirEmptyArgumentList
        annotationTypeRef = buildResolvedTypeRef {
            coneType = jsExportIgnoreAnnotation.defaultType()
        }
        calleeReference = buildResolvedNamedReference {
            name = jsExportIgnoreAnnotation.name
            resolvedSymbol = jsExportIgnoreConstructor
        }

        containingDeclarationSymbol = this@excludeFromJsExport.symbol
    }

    replaceAnnotations(annotations + jsExportIgnoreAnnotationCall)
}

fun FirDeclaration.jvmSynthetic(session: FirSession) {
    if (!session.moduleData.platform.isJvm()) {
        return
    }

    val jvmSynthetic = session.symbolProvider.getClassLikeSymbolByClassId(jvmSynthetic)
    val jvmExportIgnoreAnnotation = jvmSynthetic as? FirRegularClassSymbol ?: return
    val jvmExportIgnoreConstructor =
        jvmExportIgnoreAnnotation.declarationSymbols.firstIsInstanceOrNull<FirConstructorSymbol>() ?: return

    val jvmSyntheticAnnotationCall = buildAnnotationCall {
        argumentList = FirEmptyArgumentList
        annotationTypeRef = buildResolvedTypeRef {
            coneType = jvmExportIgnoreAnnotation.defaultType()
        }
        calleeReference = buildResolvedNamedReference {
            name = jvmExportIgnoreAnnotation.name
            resolvedSymbol = jvmExportIgnoreConstructor
        }

        containingDeclarationSymbol = this@jvmSynthetic.symbol
    }

    replaceAnnotations(annotations + jvmSyntheticAnnotationCall)
}
