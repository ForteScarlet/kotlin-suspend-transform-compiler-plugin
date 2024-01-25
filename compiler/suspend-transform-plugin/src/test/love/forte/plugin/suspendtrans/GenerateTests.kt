package love.forte.plugin.suspendtrans
import love.forte.plugin.suspendtrans.runners.AbstractCodeGenTestRunner
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

private const val PROJECT_ROOT = "compiler/suspend-transform-plugin"

fun main() {
    println("generating test class...")
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testsRoot = "$PROJECT_ROOT/src/test-gen", testDataRoot = "$PROJECT_ROOT/src/testData") {
            testClass<AbstractCodeGenTestRunner> {
                model(relativeRootPath = "codegen")
            }

        }
    }
}
