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
    fun testDecode() {
        assertEquals("0801", SuspendTransformConfiguration(emptyMap()).encodeToHex())

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
