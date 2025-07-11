package love.forte.suspendtrans.test.runner

@OptIn(markerClass = [ExperimentalMultiplatform::class])
@Retention(value = AnnotationRetention.SOURCE)
actual annotation class JsResultPromise<T> actual constructor(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean
)