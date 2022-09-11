package love.forte.plugin.suspendtrans.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import love.forte.plugin.suspendtrans.SuspendTransformComponentRegistrar
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


/**
 *
 * @author ForteScarlet
 */
class SuspendTransTest {
    @Suppress("ConvertToStringTemplate")
    private val main = SourceFile.kotlin(
        "Main.kt", """
 import kotlinx.coroutines.runBlocking
class JustTest {
    // @JvmSynthetic
    @love.forte.plugin.suspendtrans.annotation.Suspend2JvmBlocking
    suspend fun getValue(): Long {
        kotlinx.coroutines.delay(5)
        return System.currentTimeMillis()
    }

    fun getValueBlocking2(): Long = runBlocking { getValue() }

}

fun main() {
    val test = JustTest()
    println(runBlocking { test.getValue() })
    println(test.getValueBlocking2())
    JustTest::class.java.declaredMethods.forEach {
        println(it)
        if (it.name == "getValueBlocking") {
            val invoke = it.invoke(test)
            println("blocking value: " + invoke)
        }
     }
}
"""
    )
    
    @Test
    fun `IR plugin enabled`() {
        val result = compile(
            sourceFile = main,
            SuspendTransformComponentRegistrar(true)
        )
        
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    
        println(result.outputDirectory)
    
        println("======== JustTest java code ========")
        println(result.javaCode("JustTest"))
        println("===========================")
        
        println("======== MainKt java code ========")
        println(result.javaCode("MainKt"))
        println("===========================")
        
        result.classLoader.loadClass("MainKt").declaredMethods.forEach {
            println("MainKt method: $it")
        }
        
        val out = invokeMain(result, "MainKt").trim().split("""\r?\n+""".toRegex())
        println("======== invoke main result ========")
        out.forEach(::println)
    }
    
}