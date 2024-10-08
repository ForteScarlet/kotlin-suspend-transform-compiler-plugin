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
    val asProperty: Boolean = false
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
    val asProperty: Boolean = false
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
    val asProperty: Boolean = false
)
