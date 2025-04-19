package love.forte.plugin.suspendtrans.cli

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration.Companion.serializer

// Cli Options for gradle plugin

@OptIn(ExperimentalSerializationApi::class)
private val SERIALIZER = ProtoBuf {
    encodeDefaults = true
}

@OptIn(ExperimentalSerializationApi::class)
fun SuspendTransformConfiguration.encodeToHex(): String {
    return SERIALIZER.encodeToHexString(serializer(), this)
}

@OptIn(ExperimentalSerializationApi::class)
fun decodeSuspendTransformConfigurationFromHex(hex: String): SuspendTransformConfiguration {
    return SERIALIZER.decodeFromHexString(serializer(), hex)
}
