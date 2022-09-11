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
    private val main = SourceFile.kotlin(
        "Main.kt", """
 import kotlinx.coroutines.runBlocking
open class JustTest : ITest {
    // @JvmSynthetic
    @love.forte.plugin.suspendtrans.annotation.Suspend2JvmBlocking
    @love.forte.plugin.suspendtrans.annotation.Suspend2JvmAsync
    open suspend fun getValue(): Long = System.currentTimeMillis()

    @love.forte.plugin.suspendtrans.annotation.Suspend2JvmBlocking
    @love.forte.plugin.suspendtrans.annotation.Suspend2JvmAsync
    override suspend fun invoke(name: String, value: Int): Bar = Bar()
}

interface ITest {
    @love.forte.plugin.suspendtrans.annotation.Suspend2JvmBlocking
    @love.forte.plugin.suspendtrans.annotation.Suspend2JvmAsync
    suspend fun invoke(name: String, value: Int): Foo
}

open class Foo
open class Bar : Foo()

fun main() {
    val test = JustTest()
    println(runBlocking { test.getValue() })
    JustTest::class.java.declaredMethods.forEach {
        println(it)
        // if (it.name == "getValueBlocking") {
        //     val invoke = it.invoke(test)
        //     println("blocking value: " + invoke)
        // }
        // if (it.name == "getValueAsync") {
        //     val invoke = it.invoke(test)
        //     println("async value: " + invoke)
        // }
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
        
    
        println("======== JustTest java code ========")
        println(result.javaCode("ITest"))
        println("===========================")
        
        // println("======== MainKt java code ========")
        // println(result.javaCode("MainKt"))
        // println("===========================")
        
        // result.classLoader.loadClass("MainKt").declaredMethods.forEach {
        //     println("MainKt method: $it")
        // }
        
        val out = invokeMain(result, "MainKt").trim().split("""\r?\n+""".toRegex())
        println("======== invoke main result ========")
        out.forEach(::println)
    }
    
}