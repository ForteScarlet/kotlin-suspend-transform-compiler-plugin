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

    /**
     * The name of [@JsName][kotlin.js.JsName].
     * Valid when not empty.
     *
     * If [markName] is valid, [@JsName][kotlin.js.JsName] will be annotated on the generated function.
     *
     * For example:
     *
     * ```Kotlin
     * @JsPromise(markName = "markName_foo")
     * suspend fun foo(): String = "..."
     *
     * // The generated fun:
     * @JsName(name = "markName_foo")
     * fun fooAsync(): Promise<out String> = runAsync { foo() }
     * ```
     *
     *
     * @since 0.13.0
     */
    actual val markName: String = "",
)
