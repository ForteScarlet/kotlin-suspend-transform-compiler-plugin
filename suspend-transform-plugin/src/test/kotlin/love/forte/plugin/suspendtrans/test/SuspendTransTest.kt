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
        "Main.kt",
        """
 import kotlinx.coroutines.runBlocking
 import love.forte.plugin.suspendtrans.annotation.JvmAsync
 import love.forte.plugin.suspendtrans.annotation.JvmBlocking

annotation class Hello

open class JustTest { //  : ITest
    // @JvmSynthetic
    @JvmBlocking
    @JvmAsync
    @Hello
    open suspend fun value(): Long = System.currentTimeMillis()
    
    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun value2(): Long = System.currentTimeMillis()

    // @JvmAsync
    // @JvmBlocking
    // override suspend fun invoke(name: String, value: Int): Bar = Bar()
}

interface ITest {
    @JvmAsync
    @JvmBlocking
    suspend fun invoke(name: String, value: Int): Foo
    @JvmAsync(asProperty = true)
    @JvmBlocking(asProperty = true)
    suspend fun invoke2(name: String, value: Int): Foo
}

open class Foo
open class Bar : Foo()

fun main() {
    val test = JustTest()
    println(runBlocking { test.value() })
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