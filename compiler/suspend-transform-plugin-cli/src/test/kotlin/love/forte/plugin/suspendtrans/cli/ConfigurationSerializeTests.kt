package love.forte.plugin.suspendtrans.cli

import love.forte.plugin.suspendtrans.configuration.InternalSuspendTransformConfigurationApi
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jsPromiseTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmAsyncTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmBlockingTransformer
import love.forte.plugin.suspendtrans.configuration.TargetPlatform
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * @author ForteScarlet
 */
class ConfigurationSerializeTests {
    @OptIn(InternalSuspendTransformConfigurationApi::class)
    @Test
    fun testDecodeNull() {
        assertEquals(
            SuspendTransformConfiguration(emptyMap()),
            decodeSuspendTransformConfigurationFromHex("")
        )
    }

    @OptIn(InternalSuspendTransformConfigurationApi::class)
    @Test
    fun testDecodeEmpty() {
        val config = SuspendTransformConfiguration(emptyMap())
        assertEquals("", config.encodeToHex())
    }

    @OptIn(InternalSuspendTransformConfigurationApi::class)
    @Test
    fun testDecodeSingle() {
        val config = SuspendTransformConfiguration(
            transformers = mapOf(
                TargetPlatform.JVM to listOf(jvmBlockingTransformer)
            )
        )
        val hex = config.encodeToHex()
        assertEquals(config, decodeSuspendTransformConfigurationFromHex(hex))
    }

    @OptIn(InternalSuspendTransformConfigurationApi::class)
    @Test
    fun testDecodeInvalid() {
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            decodeSuspendTransformConfigurationFromHex("invalid-hex")
        }
    }

    @OptIn(InternalSuspendTransformConfigurationApi::class)
    @Test
    fun testDecode() {
        assertEquals(
            "",
            SuspendTransformConfiguration(emptyMap()).encodeToHex()
        )

        val config = SuspendTransformConfiguration(
            transformers = mapOf(
                TargetPlatform.JVM to listOf(jvmBlockingTransformer, jvmAsyncTransformer),
                TargetPlatform.JS to listOf(jsPromiseTransformer),
            )
        )

        val hex = config.encodeToHex()

        assertEquals(
            config,
            decodeSuspendTransformConfigurationFromHex(hex)
        )
    }

}
