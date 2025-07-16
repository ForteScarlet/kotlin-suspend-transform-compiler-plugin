package com.example.annotation

/**
 * A generic annotation that will be used to reproduce the issue with FirDeclarationPredicateRegistrar.registerPredicates().
 *
 * The issue occurs when using an annotation with a generic type parameter (T in this case) and implementing
 * FirDeclarationPredicateRegistrar.registerPredicates() in a FirDeclarationGenerationExtension.
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
public expect annotation class GenericAnnotation<T>(
    val name: String = "",
    val enabled: Boolean = true
)
