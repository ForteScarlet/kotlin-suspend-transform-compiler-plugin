package love.forte.plugin.suspendtrans.annotation

/**
 * The API related to _return type override generic_ is experimental.
 * It may be changed in the future without notice.
 *
 * @since 0.14.0
 */
@RequiresOptIn(
    message = "This API is experimental. It may be changed in the future without notice.",
    level = RequiresOptIn.Level.ERROR
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class ExperimentalReturnTypeOverrideGenericApi