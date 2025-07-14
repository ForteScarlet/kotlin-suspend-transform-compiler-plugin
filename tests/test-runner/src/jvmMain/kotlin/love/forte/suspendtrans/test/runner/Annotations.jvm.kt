package love.forte.suspendtrans.test.runner

@OptIn(markerClass = [ExperimentalMultiplatform::class])
@Retention(value = AnnotationRetention.SOURCE)
actual annotation class JvmResultBlock<T> actual constructor(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean
)

@OptIn(markerClass = [ExperimentalMultiplatform::class])
@Retention(value = AnnotationRetention.SOURCE)
actual annotation class JvmResultAsync<T> actual constructor(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean
)

@OptIn(markerClass = [ExperimentalMultiplatform::class])
@Retention(value = AnnotationRetention.SOURCE)
actual annotation class JvmResBlock<T> actual constructor(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean
)