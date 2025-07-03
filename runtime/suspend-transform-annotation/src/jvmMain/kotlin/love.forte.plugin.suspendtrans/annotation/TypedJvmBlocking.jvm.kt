package love.forte.plugin.suspendtrans.annotation

@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JvmBlockingWithType<T>(
    actual val type: String = "",
    actual val baseName: String = "",
    actual val suffix: String = "Blocking",
    actual val asProperty: Boolean = false,
    actual val markName: String = "",
)
