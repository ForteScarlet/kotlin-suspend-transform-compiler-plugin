package love.forte.plugin.suspendtrans.annotation

/**
 * @since *-0.10.0
 */
@Retention(AnnotationRetention.SOURCE)
@Deprecated("Only used by auto-generate", level = DeprecationLevel.HIDDEN)
@Repeatable
public annotation class TargetMarker(
    val value: String
)
