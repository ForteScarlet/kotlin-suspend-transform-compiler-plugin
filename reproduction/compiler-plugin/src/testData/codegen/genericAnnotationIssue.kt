// FIR_DUMP
// ISSUE: Symbol not found for T in generic annotation

import com.example.annotation.GenericAnnotation

/**
 * This test demonstrates the issue with generic annotations when using FirDeclarationPredicateRegistrar.registerPredicates().
 * 
 * The issue occurs when using an annotation with a generic type parameter (T in this case) and implementing
 * FirDeclarationPredicateRegistrar.registerPredicates() in a FirDeclarationGenerationExtension.
 * 
 * Expected error: ERROR CLASS: Symbol not found for T
 */

// Function with generic annotation
@GenericAnnotation<String>(name = "test", enabled = true)
fun testFunction(): String {
    return "Hello, World!"
}

// Class with generic annotation
@GenericAnnotation<Int>(name = "testClass", enabled = true)
class TestClass {
    // Method with generic annotation
    @GenericAnnotation<Boolean>(name = "testMethod", enabled = true)
    fun testMethod(): Boolean {
        return true
    }
}
