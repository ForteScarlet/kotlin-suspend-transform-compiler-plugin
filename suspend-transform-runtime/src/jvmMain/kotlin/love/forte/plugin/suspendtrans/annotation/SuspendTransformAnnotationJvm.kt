package love.forte.plugin.suspendtrans.annotation

@RequiresOptIn(message = "Api should be used by Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public actual annotation class Api4J


@RequiresOptIn(message = "Experimental jvm api", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalJvmApi

/**
 *
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public actual annotation class Suspend2JvmBlocking(actual val baseName: String, actual val suffix: String)

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
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public actual annotation class Suspend2JvmAsync(actual val baseName: String, actual val suffix: String)
