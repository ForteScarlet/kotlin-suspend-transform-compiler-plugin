package love.forte.suspendtrans.test.runner

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.SOURCE)
expect annotation class JvmResultBlock<T>(
    val baseName: String = "",
    val suffix: String = "Blocking",
    val asProperty: Boolean = false
)

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
expect annotation class JvmResBlock<T>(
    val baseName: String = "",
    val suffix: String = "Blocking",
    val asProperty: Boolean = false
)

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
expect annotation class JvmResultAsync<T>(
    val baseName: String = "",
    val suffix: String = "Async",
    val asProperty: Boolean = false
)

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
expect annotation class JsResultPromise<T>(
    val baseName: String = "",
    val suffix: String = "Async",
    val asProperty: Boolean = false
)