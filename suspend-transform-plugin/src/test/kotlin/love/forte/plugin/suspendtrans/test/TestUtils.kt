/*
 * Copyright (C) 2020 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package love.forte.plugin.suspendtrans.test

import com.strobel.assembler.InputTypeLoader
import com.strobel.assembler.metadata.ArrayTypeLoader
import com.strobel.assembler.metadata.CompositeTypeLoader
import com.strobel.assembler.metadata.ITypeLoader
import com.strobel.decompiler.Decompiler
import com.strobel.decompiler.DecompilerSettings
import com.strobel.decompiler.PlainTextOutput
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException


fun compile(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar,
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = sourceFiles
        useIR = true
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
        workingDir = File("./build-test")
    }.compile()
}

fun compile(
    sourceFile: SourceFile,
    plugin: ComponentRegistrar,
): KotlinCompilation.Result {
    return compile(listOf(sourceFile), plugin)
}

// fun assertFunction(javaCode: String, functionStatement: String, expectedFunction: String) {
//   Assertions.assertEquals(expectedFunction, fetchMethodByPrefix(javaCode, functionStatement))
// }
//
// fun fetchMethodByPrefix(classText: String, methodSignaturePrefix: String): String {
//   val classLines = classText.split("\n")
//   val methodSignaturePredicate: (String) -> Boolean = { line -> line.contains(methodSignaturePrefix) }
//   val methodFirstLineIndex = classLines.indexOfFirst(methodSignaturePredicate)
//
//   check(methodFirstLineIndex != -1) {
//     "Method with prefix '$methodSignaturePrefix' not found within class:\n$classText"
//   }
//
//   val multiplePrefixMatches = classLines
//     .indexOfFirst(methodFirstLineIndex + 1, methodSignaturePredicate)
//     .let { index -> index != -1 }
//
//   check(!multiplePrefixMatches) {
//     "Multiple methods with prefix '$methodSignaturePrefix' found within class:\n$classText"
//   }
//
//   val indentationSize = classLines[methodFirstLineIndex].takeWhile { it == ' ' }.length
//
//   var curleyBraceCount = 1
//   var currentLineIndex: Int = methodFirstLineIndex + 1
//
//   while (curleyBraceCount != 0 && currentLineIndex < classLines.lastIndex) {
//     if (classLines[currentLineIndex].contains("{")) {
//       curleyBraceCount++
//     }
//     if (classLines[currentLineIndex].contains("}")) {
//       curleyBraceCount--
//     }
//     currentLineIndex++
//   }
//
//   return classLines
//     .subList(methodFirstLineIndex, currentLineIndex)
//     .joinToString("\n") { it.substring(indentationSize) }
// }


fun invokeMain(result: KotlinCompilation.Result, className: String): String {
    val oldOut = System.out
    try {
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer, false, "UTF-8"))
        
        try {
            val kClazz = result.classLoader.loadClass(className)
            val main = kClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
            main.invoke(null)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
        
        return buffer.toString("UTF-8")
    } finally {
        System.setOut(oldOut)
    }
}

fun KotlinCompilation.Result.kotlinCode(className: String): String {
    this.generatedFiles.forEach {
        println(it)
    }
    DecompilerSettings().apply {
        this.javaFormattingOptions
    }
    
    
    TODO()
}

fun KotlinCompilation.Result.javaCode(className: String): String {
    val decompilerSettings = DecompilerSettings.javaDefaults().apply {
        typeLoader = CompositeTypeLoader(*(mutableListOf<ITypeLoader>()
            .apply {
                // Ensure every class is available.
                generatedFiles.forEach {
                    add(ArrayTypeLoader(it.readBytes()))
                }
                
                // Loads any standard classes already on the classpath.
                add(InputTypeLoader())
            }
            .toTypedArray()))
        
        isUnicodeOutputEnabled = true
    }
    
    return StringWriter().let { writer ->
        // ClassFileDecompilers.Full
        // ClassFileDecompilers.Decompiler.decompile(
        Decompiler.decompile(
            className,
            PlainTextOutput(writer).apply { isUnicodeOutputEnabled = true },
            decompilerSettings
        )
        writer.toString().trimEnd().trimIndent()
    }
}
