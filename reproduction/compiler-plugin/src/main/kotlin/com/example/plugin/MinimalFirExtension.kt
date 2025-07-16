package com.example.plugin

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.name.FqName

/**
 * Minimal FIR extension that implements FirDeclarationPredicateRegistrar.registerPredicates()
 * to reproduce the issue with generic annotations.
 */
class MinimalFirExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {

    // This is the predicate that checks for the presence of our annotation
    private val annotationPredicate = DeclarationPredicate.create {
        hasAnnotated(setOf(FqName("com.example.annotation.GenericAnnotation")))
    }

    // This is the method that causes the issue with generic annotations
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(annotationPredicate)
    }
}
