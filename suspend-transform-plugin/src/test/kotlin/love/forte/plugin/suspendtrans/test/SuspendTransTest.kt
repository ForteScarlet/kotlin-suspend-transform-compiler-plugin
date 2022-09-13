package love.forte.plugin.suspendtrans.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import love.forte.plugin.suspendtrans.SuspendTransformComponentRegistrar
import org.junit.jupiter.api.Test
import java.util.concurrent.Future
import kotlin.test.assertEquals

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

// annotation class Hello

class JustTest { //  : ITest
    @JvmBlocking
    @JvmAsync
    suspend fun value(): Bar {
        value("abc")
        return Bar()
    }

    @JvmBlocking
    @JvmAsync
    suspend fun value(value: String): Foo = Foo()

    // @JvmBlocking
    // @JvmAsync
    // suspend fun value0(v: Long): Long = v
}

class Foo
class Bar
"""
    )
    
    @Test
    fun `IR plugin JVM`() {
        val result = compileJvm(
            sourceFile = main,
            SuspendTransformComponentRegistrar(true)
        )
        
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        
        println(result.outputDirectory)
        
        println("======== JustTest java code ========")
        println(result.javaCode("JustTest"))
        println("===========================")
        
        
        // println("======== JustTest java code ========")
        // println(result.javaCode("ITest"))
        // println("===========================")
        
        // println("======== MainKt java code ========")
        // println(result.javaCode("MainKt"))
        // println("===========================")
        
        // result.classLoader.loadClass("MainKt").declaredMethods.forEach {
        //     println("MainKt method: $it")
        // }
    
        val justTest = result.classLoader.loadClass("JustTest")
        println("justTest = $justTest")
        val method = justTest.getMethod("valueAsync")
        println("method = $method")
        val justTestInstance = justTest.getConstructor().newInstance()
        println("justTestInstance = $justTestInstance")
        val invokeResult = method.invoke(justTestInstance)
        println("invokeResult: " + invokeResult)
        println("invokeResult.type: " + invokeResult::class)
        invokeResult as Future<*>
        println("invokeResult.get()" + invokeResult.get())
        println("invokeResult.get().type" + invokeResult.get()::class)
        // val out = invokeMain(result, "MainKt").trim().split("""\r?\n+""".toRegex())
        // println("======== invoke main result ========")
        // out.forEach(::println)
    }
    
    // @Test
    fun `IR plugin JS`() {
        val result = compileJs(
            sourceFile = main,
            SuspendTransformComponentRegistrar(true)
        )
    
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    
        println(result.messages)
        println(result.compiledClassAndResourceFiles)
    
    }
    
}