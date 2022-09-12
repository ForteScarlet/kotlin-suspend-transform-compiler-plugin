package love.forte.plugin.suspendtrans.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import love.forte.plugin.suspendtrans.SuspendTransformComponentRegistrar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class SuspendTransTest {
    private val main = SourceFile.kotlin(
        "Main.kt",
        """
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

annotation class Hello

open class JustTest { //  : ITest

    fun hello(): String = "Hello"
    suspend fun world(): String = "World"

    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    open suspend fun value(): Long = 114

    @JvmBlocking(asProperty = true)
    @JvmAsync
    open suspend fun value2(): Long = 514

    @JvmBlocking
    @JvmAsync(asProperty = true)
    open suspend fun value3(): Long = 810
}
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