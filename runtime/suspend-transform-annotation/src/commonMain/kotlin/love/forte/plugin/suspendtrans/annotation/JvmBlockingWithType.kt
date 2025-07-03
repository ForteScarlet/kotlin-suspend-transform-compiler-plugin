package love.forte.plugin.suspendtrans.annotation

@OptIn(ExperimentalMultiplatform::class)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@OptionalExpectation
public expect annotation class JvmBlockingWithType<T>(
    val type: String = "",
    val baseName: String = "",
    val suffix: String = "Blocking",
    val asProperty: Boolean = false,
    val markName: String = "",
)
