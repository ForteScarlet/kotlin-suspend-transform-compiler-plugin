package love.forte.plugin.suspendtrans.test

import com.bennyhuo.kotlin.compiletesting.extensions.module.KotlinModule
import com.bennyhuo.kotlin.compiletesting.extensions.module.compileAll
import com.bennyhuo.kotlin.compiletesting.extensions.source.FileBasedModuleInfoLoader
import com.tschuchort.compiletesting.KotlinCompilation
import love.forte.plugin.suspendtrans.CliOptions
import love.forte.plugin.suspendtrans.SuspendTransformComponentRegistrar
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class CpTest {

    @Test
    fun basicTest() {
        testBase("basic.kt")
    }

    @Test
    fun overrideTest() {
        testBase("override.kt")
    }

    @Test
    fun asPropertyTest() {
        testBase("asProperty.kt")
    }

    @Test
    fun typeAttrTest() {
        testBase("typeAttr.kt")
    }

    @Test
    fun testCliOptions() {
        val testIncludeAnnotations = listOf(
            SuspendTransformConfiguration.IncludeAnnotation("Repeatable", true),
            SuspendTransformConfiguration.IncludeAnnotation("Simple", false),
        )
        val toValue = SuspendTransformConfiguration().apply {
            jvm {
                originFunctionIncludeAnnotations = testIncludeAnnotations
            }
        }

        val value = CliOptions.Jvm.ORIGIN_FUNCTION_INCLUDE_ANNOTATIONS.resolveToValue(toValue)

        val fromValue = SuspendTransformConfiguration()

        CliOptions.Jvm.ORIGIN_FUNCTION_INCLUDE_ANNOTATIONS.resolveFromValue(fromValue, value)

        assertEquals(toValue.jvm.originFunctionIncludeAnnotations, fromValue.jvm.originFunctionIncludeAnnotations)

    }

    private fun testBase(fileName: String) {
        val loader = FileBasedModuleInfoLoader("testData/$fileName")
        val sourceModuleInfos = loader.loadSourceModuleInfos()

        val modules = sourceModuleInfos.map {
            KotlinModule(it, componentRegistrars = listOf(SuspendTransformComponentRegistrar())).apply {
                //compilation.apply {
                //    workingDir = File("build/em-jvm/${fileName.substringBeforeLast(".")}")
                //    useIR = true
                //    javaParameters = true
                //    jvmDefault = "all"
                //}
            }
        }

//        modules.resolveAllDependencies()
        modules.compileAll()

        modules.forEach { module ->
            val result = module.compileResult!!
            assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
            //println(" ==== JAVA CODE : ${result.generatedFiles} ====")
            //println(result.javaCode("JustTest"))
            //println(" ===================")
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