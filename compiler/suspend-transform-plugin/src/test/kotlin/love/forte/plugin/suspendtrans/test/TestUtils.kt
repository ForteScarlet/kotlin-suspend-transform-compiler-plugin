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
import com.tschuchort.compiletesting.KotlinJsCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException


fun compileJvm(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar,
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = sourceFiles
        useIR = true
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
        workingDir = File("build/em-jvm")
    }.compile()
}

fun compileJvm(
    sourceFile: SourceFile,
    plugin: ComponentRegistrar,
): KotlinCompilation.Result {
    return compileJvm(listOf(sourceFile), plugin)
}

fun compileJs(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar,
): KotlinJsCompilation.Result {
    return KotlinJsCompilation().apply {
        sources = sourceFiles
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
        workingDir = File("build/em-js")
    }.compile()
}

fun compileJs(
    sourceFile: SourceFile,
    plugin: ComponentRegistrar,
): KotlinJsCompilation.Result {
    return compileJs(listOf(sourceFile), plugin)
}

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
