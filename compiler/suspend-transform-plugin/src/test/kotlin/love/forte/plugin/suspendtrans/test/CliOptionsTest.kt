package love.forte.plugin.suspendtrans.test

import love.forte.plugin.suspendtrans.CliOptions
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class CliOptionsTest {

    @Test
    fun formatTest() {
        val annotations = listOf(
            SuspendTransformConfiguration.IncludeAnnotation("Hello", false),
            SuspendTransformConfiguration.IncludeAnnotation("World", true)
        )

        val config = SuspendTransformConfiguration().apply {
            jvm {
                syntheticBlockingFunctionIncludeAnnotations = annotations
            }
        }

        CliOptions.Jvm.SYNTHETIC_BLOCKING_FUNCTION_INCLUDE_ANNOTATIONS.also { opt ->
            val value = opt.resolveToValue(config)
            val newConfig = SuspendTransformConfiguration()
            opt.resolveFromValue(newConfig, value)
            assertEquals(annotations, newConfig.jvm.syntheticBlockingFunctionIncludeAnnotations)
        }


    }


}