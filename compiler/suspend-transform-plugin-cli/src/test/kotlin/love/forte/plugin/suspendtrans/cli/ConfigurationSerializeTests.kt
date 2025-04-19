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
            "0aa807080112bd030a680a3c0a296c6f76652e666f7274652e706c7567696e2e" +
                    "73757370656e647472616e732e616e6e6f746174696f6e120b4a766d426c6f63" +
                    "6b696e67180020001208626173654e616d651a06737566666978220a61735072" +
                    "6f70657274792a08426c6f636b696e67300012390a266c6f76652e666f727465" +
                    "2e706c7567696e2e73757370656e647472616e732e72756e74696d65120f2472" +
                    "756e496e426c6f636b696e672420002a240a1e0a0a6b6f746c696e2e6a766d12" +
                    "0c4a766d53796e7468657469631800200010001800323c0a360a296c6f76652e" +
                    "666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f7461" +
                    "74696f6e1205417069344a18002000100018013801421e0a0a6b6f746c696e2e" +
                    "6a766d120c4a766d53796e74686574696318002000423c0a296c6f76652e666f" +
                    "7274652e706c7567696e2e73757370656e647472616e732e616e6e6f74617469" +
                    "6f6e120b4a766d426c6f636b696e671800200042390a296c6f76652e666f7274" +
                    "652e706c7567696e2e73757370656e647472616e732e616e6e6f746174696f6e" +
                    "12084a766d4173796e631800200042130a066b6f746c696e12054f7074496e18" +
                    "002000480012e3030a620a390a296c6f76652e666f7274652e706c7567696e2e" +
                    "73757370656e647472616e732e616e6e6f746174696f6e12084a766d4173796e" +
                    "63180020001208626173654e616d651a06737566666978220a617350726f7065" +
                    "7274792a054173796e63300012360a266c6f76652e666f7274652e706c756769" +
                    "6e2e73757370656e647472616e732e72756e74696d65120c2472756e496e4173" +
                    "796e63241a2d0a146a6176612e7574696c2e636f6e63757272656e741211436f" +
                    "6d706c657461626c654675747572651800200020012a240a1e0a0a6b6f746c69" +
                    "6e2e6a766d120c4a766d53796e7468657469631800200010001800323c0a360a" +
                    "296c6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e" +
                    "616e6e6f746174696f6e1205417069344a18002000100018013801421e0a0a6b" +
                    "6f746c696e2e6a766d120c4a766d53796e74686574696318002000423c0a296c" +
                    "6f76652e666f7274652e706c7567696e2e73757370656e647472616e732e616e" +
                    "6e6f746174696f6e120b4a766d426c6f636b696e671800200042390a296c6f76" +
                    "652e666f7274652e706c7567696e2e73757370656e647472616e732e616e6e6f" +
                    "746174696f6e12084a766d4173796e631800200042130a066b6f746c696e1205" +
                    "4f7074496e1800200048000ad202080212cd020a630a3a0a296c6f76652e666f" +
                    "7274652e706c7567696e2e73757370656e647472616e732e616e6e6f74617469" +
                    "6f6e12094a7350726f6d697365180020001208626173654e616d651a06737566" +
                    "666978220a617350726f70657274792a054173796e63300012360a266c6f7665" +
                    "2e666f7274652e706c7567696e2e73757370656e647472616e732e72756e7469" +
                    "6d65120c2472756e496e4173796e63241a180a096b6f746c696e2e6a73120750" +
                    "726f6d697365180020002001323d0a370a296c6f76652e666f7274652e706c75" +
                    "67696e2e73757370656e647472616e732e616e6e6f746174696f6e1206417069" +
                    "344a7318002000100018013801423a0a296c6f76652e666f7274652e706c7567" +
                    "696e2e73757370656e647472616e732e616e6e6f746174696f6e12094a735072" +
                    "6f6d6973651800200042130a066b6f746c696e12054f7074496e180020004800",
            hex
        )

        assertEquals(
            config,
            decodeSuspendTransformConfigurationFromHex(hex)
        )
    }

}
