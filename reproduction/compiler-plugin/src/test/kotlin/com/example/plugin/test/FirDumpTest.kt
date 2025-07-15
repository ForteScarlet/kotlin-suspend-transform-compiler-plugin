package com.example.plugin.test

import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_PARSER
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirDumpHandler
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.JvmEnvironmentConfigurator
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Test class that demonstrates how to dump the FIR structure.
 */
class FirDumpTest : AbstractKotlinCompilerTest() {
    
    override fun configure(builder: TestConfigurationBuilder) {
        builder.globalDefaults {
            frontend = FrontendKinds.FIR
        }
        
        builder.defaultDirectives {
            FIR_PARSER with FirParser.LightTree
        }
        
        builder.configureFirHandlersStep {
            useHandlers(::FirDumpHandler)
        }
        
        builder.useConfigurators(
            ::CommonEnvironmentConfigurator,
            ::JvmEnvironmentConfigurator,
            ::MinimalPluginConfigurator
        )
    }
    
    @Test
    fun testFirDump() {
        // Create a temporary file with code that uses the GenericAnnotation
        val testFile = File.createTempFile("test", ".kt").apply {
            writeText("""
                import com.example.annotation.GenericAnnotation
                
                @GenericAnnotation<String>(name = "test", enabled = true)
                fun testFunction(): String {
                    return "Hello, World!"
                }
            """.trimIndent())
            deleteOnExit()
        }
        
        // Run the test on the file
        runTest(testFile.absolutePath)
        
        // The FIR dump will be written to the build directory
        println("FIR dump has been generated. Check the build directory for the output.")
    }
}

/**
 * Configurator for the minimal compiler plugin.
 */
class MinimalPluginConfigurator : CommonEnvironmentConfigurator() {
    override fun configureCompilerConfiguration(configuration: org.jetbrains.kotlin.config.CompilerConfiguration, module: org.jetbrains.kotlin.test.model.TestModule) {
        // Configure the compiler plugin
        // This is where you would add the plugin to the compiler configuration
    }
}
