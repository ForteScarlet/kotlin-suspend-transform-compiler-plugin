package love.forte.plugin.suspendtrans.annotation


@RequiresOptIn(message = "Api should be used by Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
public expect annotation class Api4J()


@RequiresOptIn(message = "Api should be used by JavaScript", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
public expect annotation class Api4Js()

/**
 *
 * ```kotlin
 * @JvmBlocking
 * suspend fun foo(): T = ...
 * ```
 * transform to:
 *
 * ```kotlin
 * @JvmSynthetic
 * suspend fun foo(): T = ...
 *
 * @Api4J
 * fun fooBlocking(): T = runInBlocking { foo() }
 * ```
 *
 */
@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class JvmBlocking(
    /**
     * The base name of synthetic function
     * or the current function name if empty.
     *
     * The final function name is: [baseName] + [suffix]
     */
    val baseName: String = "",

    /**
     * [baseName] 名称基础上追加的名称后缀。
     */
    val suffix: String = "Blocking",

    /**
     * 是否转化为 property 的形式：
     *
     * ```kotlin
     * suspend fun foo(): T = ...
     *
     * // Generated
     * val fooBlocking: T get() = runInBlocking { foo() }
     * ```
     *
     * Note: If [asProperty] == `true`, the function cannot have parameters.
     *
     */
    val asProperty: Boolean = false,

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
    val markName: String = "",
)

/**
 * ```kotlin
 * suspend fun run(): Int
 * ```
 *
 * to
 *
 * ```kotlin
 * @JvmSynthetic
 * suspend fun run(): Int
 *
 * @Api4J
 * fun runAsync(): Future<Int> = jvmAsyncScope.future { run() }
 *
 * ```
 *
 */
@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class JvmAsync(
    val baseName: String = "",
    val suffix: String = "Async",
    /**
     * 是否转化为 property 的形式：
     *
     * ```kotlin
     * suspend fun foo(): T = ...
     *
     * // Generated
     * val fooAsync: Future<T> get() = runInAsync { foo() }
     * ```
     *
     * Note: If [asProperty] == `true`, the function cannot have parameters.
     */
    val asProperty: Boolean = false,

    /**
     * The name of [@JvmName][kotlin.jvm.JvmName].
     * Valid when not empty.
     *
     * If [markName] is valid, [@JvmName][kotlin.jvm.JvmName] will be annotated on the generated function.
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
    val markName: String = "",
)


@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class JsPromise(
    val baseName: String = "",
    val suffix: String = "Async",
    /**
     * 是否转化为 property 的形式：
     *
     * ```kotlin
     * suspend fun foo(): T = ...
     *
     * // Generated
     * val fooAsync: Promise<T> get() = runInAsync { foo() }
     * ```
     *
     * Note: If [asProperty] == `true`, the function cannot have parameters.
     *
     */
    val asProperty: Boolean = false,

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
     *
     * @since 0.13.0
     */
    val markName: String = "",
)
