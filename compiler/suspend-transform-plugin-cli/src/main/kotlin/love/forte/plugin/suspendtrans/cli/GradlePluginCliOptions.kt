package love.forte.plugin.suspendtrans.cli

// Cli Options for gradle plugin

interface GradlePluginSuspendTransformCliOption<T> : SuspendTransformCliOption {
    /**
     * Encode a value [T] to a String CLI option.
     * _Should use base64(protobuf(value))_
     */
    fun encode(value: T): String
}
