package wasmtrans

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class JsPromise(
    val baseName: String = "",
    val suffix: String = "Async",
)
