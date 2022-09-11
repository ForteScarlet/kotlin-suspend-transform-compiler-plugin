package love.forte.plugin.suspendtrans.annotation

@Retention(AnnotationRetention.BINARY)
@Deprecated("Only used by auto-generate", level = DeprecationLevel.HIDDEN)
public annotation class Generated

@RequiresOptIn(message = "Api should be used by Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
public expect annotation class Api4J


@RequiresOptIn(message = "Api should be used by JavaScript", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
public expect annotation class Api4Js

/**
 *
 */
@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class Suspend2JvmBlocking(val baseName: String = "", val suffix: String = "Blocking")

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
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class Suspend2JvmAsync(val baseName: String = "", val suffix: String = "Async")


@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class Suspend2JsPromise