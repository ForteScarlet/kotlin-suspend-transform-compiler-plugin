package love.forte.plugin.suspendtrans.annotation

@RequiresOptIn(message = "Api should be used by JavaScript", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public actual annotation class Api4Js

@RequiresOptIn(message = "Experimental javascript api", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalJsApi

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public actual annotation class JsPromise(
    actual val baseName: String,
    actual val suffix: String,
    actual val asProperty: Boolean,
)
