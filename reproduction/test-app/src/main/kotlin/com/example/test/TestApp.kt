package com.example.test

import com.example.annotation.GenericAnnotation

/**
 * A simple test class that uses the GenericAnnotation with a generic type parameter.
 * 
 * When compiled with the compiler plugin that implements FirDeclarationPredicateRegistrar.registerPredicates(),
 * this will produce the error: "Symbol not found for T"
 */
class TestApp {
    /**
     * This function is annotated with GenericAnnotation<String>.
     * The compiler plugin will try to process this annotation and will encounter the issue.
     */
    @GenericAnnotation<String>(name = "test", enabled = true)
    fun testFunction(): String {
        return "Hello, World!"
    }
}

/**
 * Main function to run the application.
 */
fun main() {
    val app = TestApp()
    println(app.testFunction())
}
