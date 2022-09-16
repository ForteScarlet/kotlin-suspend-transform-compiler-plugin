package love.forte.plugin.suspendtrans.annotation

@RequiresOptIn(message = "Api should be used by Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public actual annotation class Api4J


@RequiresOptIn(message = "Experimental jvm api", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalJvmApi

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
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JvmBlocking(
    /**
     * 生成函数的基础名称，如果为空则为当前函数名。
     * 最终生成的函数名为 [baseName] + [suffix]。
     */
    actual val baseName: String,
    
    /**
     * [baseName] 名称基础上追加的名称后缀。
     */
    actual val suffix: String,
    
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
    actual val asProperty: Boolean,
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
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JvmAsync(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean,
)
