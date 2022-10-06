package love.forte.plugin.suspendtrans.test

import love.forte.plugin.suspendtrans.CliOptions
import love.forte.plugin.suspendtrans.ICliOption
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration
import kotlin.reflect.KMutableProperty1
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class CliOptionsTest {

    private fun <T> jvmConfigFormatTest(property: KMutableProperty1<SuspendTransformConfiguration.Jvm, T>, value: T, option: ICliOption) {
        val originConfig = SuspendTransformConfiguration().apply {
            property.set(jvm, value)
        }

        val valueString = option.resolveToValue(originConfig)
        val newConfig = SuspendTransformConfiguration()
        option.resolveFromValue(newConfig, valueString)
        assertEquals(value, property.get(newConfig.jvm))

    }

    @Test
    fun formatTest() {
        val annotations = listOf(
            SuspendTransformConfiguration.IncludeAnnotation("Hello", false),
            SuspendTransformConfiguration.IncludeAnnotation("World", true)
        )

        jvmConfigFormatTest(SuspendTransformConfiguration.Jvm::syntheticBlockingFunctionIncludeAnnotations, annotations, CliOptions.Jvm.SYNTHETIC_BLOCKING_FUNCTION_INCLUDE_ANNOTATIONS)
        jvmConfigFormatTest(SuspendTransformConfiguration.Jvm::syntheticAsyncFunctionIncludeAnnotations, annotations, CliOptions.Jvm.SYNTHETIC_ASYNC_FUNCTION_INCLUDE_ANNOTATIONS)

        jvmConfigFormatTest(SuspendTransformConfiguration.Jvm::originFunctionIncludeAnnotations, annotations, CliOptions.Jvm.ORIGIN_FUNCTION_INCLUDE_ANNOTATIONS)


    }


}