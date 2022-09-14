//package love.forte.plugin.suspendtrans.test
//
//import com.tschuchort.compiletesting.KotlinCompilation
//import com.tschuchort.compiletesting.SourceFile
//import SuspendTransformComponentRegistrar
//import org.junit.jupiter.api.Test
//import java.util.concurrent.CompletableFuture
//import kotlin.test.assertEquals
//
//private const val RESULT = 111
//
///**
// *
// * @author ForteScarlet
// */
//class SuspendTransTest {
//    private val main = SourceFile.kotlin(
//        "Main.kt",
//        """
//import kotlinx.coroutines.runBlocking
//import JvmAsync
//import JvmBlocking
//
//// annotation class Hello
//
//class JustTest { //  : ITest
//    @JvmBlocking
//    @JvmAsync
//    suspend fun value(): Int = value("$RESULT")
//
//    @JvmBlocking
//    @JvmAsync
//    suspend fun value(value: String): Int = value.toInt()
//}
//"""
//    )
//
//    @Test
//    fun `IR plugin JVM`() {
//        val result = compileJvm(
//            sourceFile = main,
//            SuspendTransformComponentRegistrar(true)
//        )
//
//        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
//
//        println(result.outputDirectory)
//
//        println("======== JustTest java code ========")
//        println(result.javaCode("JustTest"))
//        println("===========================")
//
//
//        // println("======== JustTest java code ========")
//        // println(result.javaCode("ITest"))
//        // println("===========================")
//
//        // println("======== MainKt java code ========")
//        // println(result.javaCode("MainKt"))
//        // println("===========================")
//
//        // result.classLoader.loadClass("MainKt").declaredMethods.forEach {
//        //     println("MainKt method: $it")
//        // }
//
//        val justTest = result.classLoader.loadClass("JustTest")
//        val justTestInstance = justTest.getConstructor().newInstance()
//
//        val blockingMethod = justTest.getMethod("valueBlocking")
//        val blockingInvokeResult = blockingMethod.invoke(justTestInstance)
//
//        assert(blockingInvokeResult == RESULT) { "Blocking invoke result $blockingInvokeResult != $RESULT" }
//
//        val asyncMethod = justTest.getMethod("valueAsync")
//        val asyncInvokeResult = asyncMethod.invoke(justTestInstance)
//        assert(asyncInvokeResult is CompletableFuture<*>) { "Async invoke result !is Future" }
//
//        asyncInvokeResult as CompletableFuture<*>
//        val asyncFutureResult = asyncInvokeResult.get()
//        assert(asyncFutureResult == RESULT) { "Async future result $blockingInvokeResult != $RESULT" }
//        // val out = invokeMain(result, "MainKt").trim().split("""\r?\n+""".toRegex())
//        // println("======== invoke main result ========")
//        // out.forEach(::println)
//    }
//
//    // @Test
//    fun `IR plugin JS`() {
//        val result = compileJs(
//            sourceFile = main,
//            SuspendTransformComponentRegistrar(true)
//        )
//
//        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
//
//        println(result.messages)
//        println(result.compiledClassAndResourceFiles)
//
//    }
//
//}