package com.example.annotation

/**
 * JVM actual implementation of the GenericAnnotation.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public actual annotation class GenericAnnotation<T> actual constructor(
    actual val name: String,
    actual val enabled: Boolean
)
