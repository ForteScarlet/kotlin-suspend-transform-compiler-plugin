package suspendtrans.test

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AB(
    val asyncBaseName: String = "",
    val asyncSuffix: String = "Async",
    val asyncAsProperty: Boolean = false,
    
    
    val blockingBaseName: String = "",
    val blockingSuffix: String = "Blocking",
    val blockingAsProperty: Boolean = false,
)
