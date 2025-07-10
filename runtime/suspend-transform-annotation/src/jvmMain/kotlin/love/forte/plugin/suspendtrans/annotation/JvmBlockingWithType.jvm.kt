package love.forte.plugin.suspendtrans.annotation

@OptIn(markerClass = [ExperimentalMultiplatform::class])
@Target(allowedTargets = [AnnotationTarget.FUNCTION, AnnotationTarget.CLASS])
@Retention(value = AnnotationRetention.BINARY)
public actual annotation class JvmBlockingWithType<T> actual constructor(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean,
    actual val markName: String
)

@OptIn(markerClass = [ExperimentalMultiplatform::class])
@Target(allowedTargets = [AnnotationTarget.FUNCTION, AnnotationTarget.CLASS])
@Retention(value = AnnotationRetention.BINARY)
public actual annotation class JvmBlockingWithType0<T> actual constructor(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean,
    actual val markName: String
)