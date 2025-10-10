/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package love.forte.plugin.suspendtrans.annotation

/**
 * Marks an API that is specifically designed for Java interoperability.
 *
 * This annotation is used to indicate that the annotated element is part of a Java-specific API
 * and should primarily be used by Java consumers rather than Kotlin code.
 *
 * When this annotation is applied to methods generated from suspend functions,
 * it signifies that these methods were created or generated specifically to provide a Java-friendly
 * alternative to Kotlin's coroutine-based APIs.
 */
@RequiresOptIn(message = "Api should be used by Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented // since 0.13.2
public actual annotation class Api4J


@RequiresOptIn(message = "Experimental jvm api", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalJvmApi

/**
 * Generate a blocking function for Java interoperability based on the current suspend function.
 *
 * When applied to a suspend function like:
 * ```kotlin
 * @JvmBlocking
 * suspend fun foo(): T = ...
 * ```
 *
 * It generates:
 * ```kotlin
 * @JvmSynthetic
 * suspend fun foo(): T = ... // Original function is hidden from Java
 *
 * @Api4J
 * fun fooBlocking(): T = runInBlocking { foo() } // New blocking function for Java
 * ```
 *
 * This annotation can be applied to both individual functions and classes
 * (in which case it applies to all suspend functions in the class).
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented // since 0.13.2
public actual annotation class JvmBlocking(
    /**
     * The base name of synthetic function
     * or the current function name if empty.
     *
     * The final function name is: [baseName] + [suffix]
     */
    actual val baseName: String,

    /**
     * The suffix to append to the [baseName] when generating the blocking function name.
     *
     * The final function name will be constructed as: [baseName] + [suffix]
     * Default value is "Blocking", resulting in names like "fooBlocking".
     */
    actual val suffix: String,

    /**
     * Specifies whether to generate a property instead of a function.
     *
     * When set to `true`, instead of generating a blocking function, a property will be created:
     *
     * ```kotlin
     * suspend fun foo(): T = ...
     *
     * // Generated property (when asProperty = true)
     * val fooBlocking: T get() = runInBlocking { foo() }
     * ```
     *
     * Note: If [asProperty] == `true`, the function cannot have parameters.
     */
    actual val asProperty: Boolean,

    /**
     * The name of `@JvmName`.
     * Valid when not empty.
     *
     * If [markName] is valid, [kotlin.jvm.JvmName] will be annotated on the generated function.
     *
     * For example:
     *
     * ```Kotlin
     * @JvmBlocking(markName = "markName_foo")
     * suspend fun foo(): String = "..."
     *
     * // The generated fun:
     * @JvmName(name = "markName_foo")
     * fun fooBlocking(): String = runBlocking { foo() }
     * ```
     *
     * Note: In the JVM, adding `@JvmName` to a non-final function is usually not allowed by the compiler.
     * @since 0.13.0
     */
    actual val markName: String = "",
)

/**
 * Generate a Future-based async function for Java interoperability based on the current suspend function.
 *
 * When applied to a suspend function like:
 * ```kotlin
 * @JvmAsync
 * suspend fun run(): Int
 * ```
 *
 * It generates:
 * ```kotlin
 * @JvmSynthetic
 * suspend fun run(): Int  // Original function is hidden from Java
 *
 * @Api4J
 * fun runAsync(): CompletableFuture<out Int> = jvmAsyncScope.future { run() }  // New async function for Java
 * ```
 *
 * This annotation allows Kotlin suspend functions to be exposed to Java code using a Future-based API.
 * It can be applied to both individual functions and classes
 * (in which case it applies to all suspend functions in the class).
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented // since 0.13.2
public actual annotation class JvmAsync(
    /**
     * The base name of the generated asynchronous function
     * or the current function name if empty.
     *
     * The final function name is: [baseName] + [suffix]
     */
    actual val baseName: String,

    /**
     * The suffix to append to the [baseName] when generating the asynchronous function name.
     *
     * The final function name will be constructed as: [baseName] + [suffix]
     * Default value is "Async", resulting in names like "fooAsync".
     */
    actual val suffix: String,

    /**
     * Specifies whether to generate a property instead of a function.
     *
     * When set to `true`, instead of generating an async function, a property will be created:
     *
     * ```kotlin
     * suspend fun foo(): T = ...
     *
     * // Generated property (when asProperty = true)
     * val fooAsync: Future<T> get() = runInAsync { foo() }
     * ```
     *
     * Note: If [asProperty] == `true`, the function cannot have parameters.
     */
    actual val asProperty: Boolean,

    /**
     * The name of [@JvmName][kotlin.jvm.JvmName].
     * Valid when not empty.
     *
     * If [markName] is valid, [@JvmName][kotlin.jvm.JvmName] will be annotated on the generated function.
     *
     * For example:
     *
     * ```Kotlin
     * @JvmAsync(markName = "markName_foo")
     * suspend fun foo(): String = "..."
     *
     * // The generated fun:
     * @JvmName(name = "markName_foo")
     * fun fooAsync(): Future<String> = jvmAsyncScope.future { foo() }
     * ```
     *
     * Note: In the JVM, adding `@JvmName` to a non-final function is usually not allowed by the compiler.
     * @since 0.13.0
     */
    actual val markName: String = "",
)
