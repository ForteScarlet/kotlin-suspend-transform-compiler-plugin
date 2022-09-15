package love.forte.plugin.suspendtrans.test

import com.bennyhuo.kotlin.compiletesting.extensions.source.SingleFileModuleInfoLoader
import com.tschuchort.compiletesting.KotlinCompilation
import love.forte.plugin.suspendtrans.SuspendTransformComponentRegistrar
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class Test {

    @Test
    fun test() {
        testBase("justTest.kt")
    }

    private fun testBase(fileName: String) {
        val loader = SingleFileModuleInfoLoader("testData/$fileName")
        val sourceModuleInfos = loader.loadSourceModuleInfos()

        val modules = sourceModuleInfos.map {
            KotlinModule(it, componentRegistrars = listOf(SuspendTransformComponentRegistrar())).apply {
                compilation.apply {
                    workingDir = File("build/em-jvm")
                    useIR = true
                }
            }
        }

        modules.resolveAllDependencies()
        modules.compileAll()

        modules.forEach { module ->
            val result = module.compileResult!!
            assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
            println(" ==== JAVA CODE : ${result.generatedFiles} ====")
            println(result.javaCode("JustTest"))
            println(" ===================")
        }

//
//        val resultMap = modules.associate {
//            it.name to it.runJvm()
//        }
//
//        println("resultMap: $resultMap")
//
//        val loadExpectModuleInfos = loader.loadExpectModuleInfos()
//
//        println("loadExpectModuleInfos: $loadExpectModuleInfos")
//
//        loadExpectModuleInfos.forEach { expectModuleInfo ->
//            println("expect module info = $expectModuleInfo")
//            expectModuleInfo.sourceFileInfos.forEach {
//                println(it.sourceBuilder)
//
//            }
//        }
//
//        ResultCollector()

//        val info = loader.loadExpectModuleInfos().fold(ResultCollector()) { collector, expectModuleInfo ->
//            collector.collectModule(expectModuleInfo.name)
//            expectModuleInfo.sourceFileInfos.forEach {
//                collector.collectFile(it.fileName)
//                collector.collectLine(it.sourceBuilder, resultMap[expectModuleInfo.name]?.get(it.fileName))
//            }
//            collector
//        }
//
//        println("info")
//        info

    }
}