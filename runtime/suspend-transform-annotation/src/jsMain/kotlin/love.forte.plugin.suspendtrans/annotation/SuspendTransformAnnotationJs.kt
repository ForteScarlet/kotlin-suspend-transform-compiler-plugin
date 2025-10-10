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
 * Marks an API that is specifically designed for JavaScript interoperability.
 *
 * This annotation is used to indicate that the annotated element is part of a JavaScript-specific API
 * and should primarily be used by JavaScript consumers rather than Kotlin code.
 *
 * When this annotation is applied to methods generated from suspend functions,
 * it signifies that these methods were created or generated specifically to provide a JavaScript-friendly
 * alternative to Kotlin's coroutine-based APIs, typically using JavaScript Promises.
 */
@RequiresOptIn(message = "Api should be used by JavaScript", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public actual annotation class Api4Js

@RequiresOptIn(message = "Experimental javascript api", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalJsApi

/**
 * Generate a Promise-based variant function for Java interoperability based on the current suspend function.
 *
 * When applied to a suspend function like:
 * ```kotlin
 * @JsPromise
 * suspend fun run(): String
 * ```
 *
 * It generates:
 * ```kotlin
 * @JvmSynthetic
 * suspend fun run(): String  // Original suspend function
 *
 * @Api4Js
 * fun runAsync(): Promise<String> = runAsync { run() }  // New Promise-based function for JavaScript
 * ```
 *
 * This annotation allows Kotlin suspend functions to be exposed to JavaScript code using JavaScript Promises.
 * It can be applied to both individual functions and classes (in which case it applies to all suspend functions in the class).
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented // since 0.13.2
public actual annotation class JsPromise(
    /**
     * The base name of the generated Promise-based function
     * or the current function name if empty.
     *
     * The final function name is: [baseName] + [suffix]
     */
    actual val baseName: String,

    /**
     * The suffix to append to the [baseName] when generating the Promise-based function name.
     *
     * The final function name will be constructed as: [baseName] + [suffix]
     * Default value is "Async", resulting in names like "fooAsync".
     */
    actual val suffix: String,

    /**
     * Specifies whether to generate a property instead of a function.
     *
     * When set to `true`, instead of generating a Promise-based function, a property will be created:
     *
     * ```kotlin
     * suspend fun foo(): T = ...
     *
     * // Generated property (when asProperty = true)
     * val fooAsync: Promise<T> get() = runInAsync { foo() }
     * ```
     *
     * Note: If [asProperty] == `true`, the function cannot have parameters.
     */
    actual val asProperty: Boolean,

    /**
     * The name of [@JsName][kotlin.js.JsName].
     * Valid when not empty.
     *
     * If [markName] is valid, [@JsName][kotlin.js.JsName] will be annotated on the generated function.
     *
     * For example:
     *
     * ```Kotlin
     * @JsPromise(markName = "markName_foo")
     * suspend fun foo(): String = "..."
     *
     * // The generated fun:
     * @JsName(name = "markName_foo")
     * fun fooAsync(): Promise<out String> = runAsync { foo() }
     * ```
     *
     * This helps control the JavaScript function name when using the Kotlin/JS compiler.
     *
     * @since 0.13.0
     */
    actual val markName: String = "",
)
