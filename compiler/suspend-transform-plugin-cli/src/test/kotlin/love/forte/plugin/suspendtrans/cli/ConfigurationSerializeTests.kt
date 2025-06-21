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
            "0aba0808011286040a95010a3c0a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e120b4a766d426c6f636b696e67180020001208626173654e616d651a06737566666978220a617350726f70657274792a08426c6f636b696e6730003a2b0a086d61726b4e616d6512190a0a6b6f746c696e2e6a766d12074a766d4e616d65180020001a046e616d6512390a266c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e72756e74696d65120f2472756e496e426c6f636b696e672420002a240a1e0a0a6b6f746c696e2e6a766d120c4a766d53796e7468657469631800200010001800323c0a360a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e1205417069344a18002000100018013801421e0a0a6b6f746c696e2e6a766d120c4a766d53796e74686574696318002000423c0a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e120b4a766d426c6f636b696e671800200042390a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e12084a766d4173796e631800200042130a066b6f746c696e12054f7074496e1800200042190a0a6b6f746c696e2e6a766d12074a766d4e616d6518002000480012ac040a8f010a390a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e12084a766d4173796e63180020001208626173654e616d651a06737566666978220a617350726f70657274792a054173796e6330003a2b0a086d61726b4e616d6512190a0a6b6f746c696e2e6a766d12074a766d4e616d65180020001a046e616d6512360a266c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e72756e74696d65120c2472756e496e4173796e63241a2d0a146a6176612e7574696c2e636f6e63757272656e741211436f6d706c657461626c654675747572651800200020012a240a1e0a0a6b6f746c696e2e6a766d120c4a766d53796e7468657469631800200010001800323c0a360a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e1205417069344a18002000100018013801421e0a0a6b6f746c696e2e6a766d120c4a766d53796e74686574696318002000423c0a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e120b4a766d426c6f636b696e671800200042390a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e12084a766d4173796e631800200042130a066b6f746c696e12054f7074496e1800200042190a0a6b6f746c696e2e6a766d12074a766d4e616d651800200048000a970308021292030a8e010a3a0a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e12094a7350726f6d697365180020001208626173654e616d651a06737566666978220a617350726f70657274792a054173796e6330003a290a086d61726b4e616d6512170a096b6f746c696e2e6a7312064a734e616d65180020001a046e616d6512360a266c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e72756e74696d65120c2472756e496e4173796e63241a180a096b6f746c696e2e6a73120750726f6d697365180020002001323d0a370a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e1206417069344a7318002000100018013801423a0a296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e12094a7350726f6d6973651800200042130a066b6f746c696e12054f7074496e1800200042170a096b6f746c696e2e6a7312064a734e616d65180020004800",
            hex
        )

        assertEquals(
            config,
            decodeSuspendTransformConfigurationFromHex(hex)
        )
    }

}
