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
     * 生成函数的基础名称，如果为空则为当前函数名。
     * 最终生成的函数名为 [baseName] + [suffix]。
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
     * 只有函数没有参数时有效。
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
     * 只有函数没有参数时有效。
     *
     */
    actual val asProperty: Boolean = false
)


@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class JsPromise()