package love.forte.plugin.suspendtrans.runners

import love.forte.plugin.suspendtrans.services.SuspendTransformerEnvironmentConfigurator
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.handlers.AsmLikeInstructionListingHandler
import org.jetbrains.kotlin.test.backend.handlers.BytecodeListingHandler
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureFirHandlersStep
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.configuration.setupJvmPipelineSteps
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.CHECK_ASM_LIKE_INSTRUCTIONS
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_PARSER
import org.jetbrains.kotlin.test.initIdeaConfiguration
import org.jetbrains.kotlin.test.model.ArtifactKinds
import org.jetbrains.kotlin.test.model.DependencyKind
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.junit.jupiter.api.BeforeAll

abstract class AbstractTestRunner : AbstractKotlinCompilerTest() {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            initIdeaConfiguration() // set system property to initialize idea service
        }
    }

    override fun configure(builder: TestConfigurationBuilder) {
        // Kotlin 2.4.20 移除了 commonConfigurationForJvmTest；统一使用官方 K2 JVM 流水线。
        builder.setupJvmPipelineSteps(FirParser.LightTree)

        builder.globalDefaults {
            targetBackend = TargetBackend.JVM_IR
            targetPlatform = JvmPlatforms.defaultJvmPlatform
            artifactKind = ArtifactKinds.Jvm
            dependencyKind = DependencyKind.Binary
        }

        builder.defaultDirectives {
            FIR_PARSER with FirParser.LightTree
            +CHECK_ASM_LIKE_INSTRUCTIONS
        }

        builder.configureHandlers()
        builder.configureFirHandlersStep {

        }

        // We need to explicitly configure JVM artifacts handlers to generate .asm files
        builder.configureJvmArtifactsHandlersStep {
            useHandlers(
                ::BytecodeListingHandler,
                ::AsmLikeInstructionListingHandler
            )
        }

        builder.useConfigurators(
            ::SuspendTransformerEnvironmentConfigurator,    // compiler plugin configuration
        )
    }

    abstract fun TestConfigurationBuilder.configureHandlers()

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }
}
