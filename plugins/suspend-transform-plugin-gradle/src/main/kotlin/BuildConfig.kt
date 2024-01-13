import love.forte.plugin.suspendtrans.gradle.SuspendTransPluginConstants

@Deprecated(
    "Use SuspendTransPluginConstants",
    ReplaceWith(
        "SuspendTransPluginConstants",
        imports = ["love.forte.plugin.suspendtrans.gradle.SuspendTransPluginConstants"]
    )
)
internal object BuildConfig {
    internal const val KOTLIN_PLUGIN_ID: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_ID

    internal const val PLUGIN_VERSION: String = SuspendTransPluginConstants.PLUGIN_VERSION

    internal const val KOTLIN_PLUGIN_GROUP: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_GROUP

    internal const val KOTLIN_PLUGIN_NAME: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_NAME

    internal const val KOTLIN_PLUGIN_VERSION: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_VERSION

    internal const val ANNOTATION_GROUP: String = SuspendTransPluginConstants.ANNOTATION_GROUP

    internal const val ANNOTATION_NAME: String = SuspendTransPluginConstants.ANNOTATION_NAME

    internal const val ANNOTATION_VERSION: String = SuspendTransPluginConstants.ANNOTATION_VERSION

    internal const val RUNTIME_GROUP: String = SuspendTransPluginConstants.RUNTIME_GROUP

    internal const val RUNTIME_NAME: String = SuspendTransPluginConstants.RUNTIME_NAME

    internal const val RUNTIME_VERSION: String = SuspendTransPluginConstants.RUNTIME_VERSION
}
