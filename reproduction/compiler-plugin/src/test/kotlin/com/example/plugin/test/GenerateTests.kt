package com.example.plugin.test

import com.example.plugin.test.runners.AbstractCodeGenTestRunner
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

private const val PROJECT_ROOT = "reproduction/compiler-plugin"

/**
 * Generates test classes based on test data files.
 */
fun main() {
    println("Generating test classes...")
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testsRoot = "$PROJECT_ROOT/src/test-gen", testDataRoot = "$PROJECT_ROOT/src/testData") {
            testClass<AbstractCodeGenTestRunner> {
                model(relativeRootPath = "codegen")
            }
        }
    }
}
