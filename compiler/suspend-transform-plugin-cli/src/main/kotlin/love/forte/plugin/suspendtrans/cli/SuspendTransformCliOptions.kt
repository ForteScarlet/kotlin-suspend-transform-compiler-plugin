package love.forte.plugin.suspendtrans.cli

object SuspendTransformCliOptions {
    const val CONFIGURATION = "configuration"

    val CLI_CONFIGURATION = SuspendTransformCliOption(
        optionName = "configuration",
        valueDescription = "Configuration hex string",
        description = "Configuration serialized protobuf hex string value",
        required = true,
        allowMultipleOccurrences = false,
    )
}
