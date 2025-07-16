package com.example.plugin.test.runners

import com.example.plugin.MinimalFirExtensionCompilerRegistrar
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.handlers.AsmLikeInstructionListingHandler
import org.jetbrains.kotlin.test.backend.handlers.BytecodeListingHandler
import org.jetbrains.kotlin.test.backend.ir.JvmIrBackendFacade
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.configuration.commonConfigurationForJvmTest
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.CHECK_ASM_LIKE_INSTRUCTIONS
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_PARSER
import org.jetbrains.kotlin.test.frontend.fir.Fir2IrResultsConverter
import org.jetbrains.kotlin.test.frontend.fir.FirFrontendFacade
import org.jetbrains.kotlin.test.initIdeaConfiguration
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.JvmEnvironmentConfigurator
import org.jetbrains.kotlin.utils.PathUtil
import org.junit.jupiter.api.BeforeAll
import java.io.File

/**
 * Base test runner for compiler tests.
 */
abstract class AbstractTestRunner : AbstractKotlinCompilerTest() {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            initIdeaConfiguration() // set system property to initialize idea service
        }
    }

    override fun configure(builder: TestConfigurationBuilder) {
        val targetFrontend: FrontendKind<*> = FrontendKinds.FIR
        builder.globalDefaults {
            frontend = targetFrontend
            targetBackend = TargetBackend.JVM_IR_SERIALIZE
            targetPlatform = JvmPlatforms.defaultJvmPlatform
            artifactKind = ArtifactKinds.Jvm
            dependencyKind = DependencyKind.Source
            backendKind = BackendKinds.IrBackendForK1AndK2
        }

        builder.defaultDirectives {
            FIR_PARSER with FirParser.LightTree
            +CHECK_ASM_LIKE_INSTRUCTIONS
        }

        builder.commonConfigurationForJvmTest(
            FrontendKinds.FIR,
            ::FirFrontendFacade,
            ::Fir2IrResultsConverter,
            ::JvmIrBackendFacade
        )

        builder.configureHandlers()

        // We need to explicitly configure JVM artifacts handlers to generate .asm files
        builder.configureJvmArtifactsHandlersStep {
            useHandlers(
                ::BytecodeListingHandler,
                ::AsmLikeInstructionListingHandler
            )
        }

        builder.useConfigurators(
            ::CommonEnvironmentConfigurator,     // compiler flags
            ::JvmEnvironmentConfigurator,        // jdk and kotlin runtime configuration
            ::MinimalEnvironmentConfigurator,
        )
    }

    abstract fun TestConfigurationBuilder.configureHandlers()

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }
}

class MinimalEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    @OptIn(ExperimentalCompilerApi::class)
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        MinimalFirExtensionCompilerRegistrar.register(this, configuration)
    }

    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        configuration.put(JVMConfigurationKeys.NO_JDK, false)
        configuration.configureJdkClasspathRoots()

        getRuntimeJarFile("com.example.annotation.GenericAnnotation")?.let {
            configuration.addJvmClasspathRoot(it)
        }

        getRuntimeJarFile("kotlinx.coroutines.CoroutineScope")?.let {
            configuration.addJvmClasspathRoot(it)
        }
    }

    private fun getRuntimeJarFile(className: String): File? {
        try {
            return getRuntimeJarFile(Class.forName(className))
        } catch (_: ClassNotFoundException) {
            System.err.println("Runtime jar '$className' not found!")
        }
        return null
    }

    private fun getRuntimeJarFile(clazz: Class<*>): File {
        return PathUtil.getResourcePathForClass(clazz)
    }
}
