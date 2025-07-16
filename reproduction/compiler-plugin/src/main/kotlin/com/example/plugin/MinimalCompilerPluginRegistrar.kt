package com.example.plugin

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
class MinimalFirExtensionCompilerRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        register(this, configuration)
    }

    companion object {
        fun register(storage: ExtensionStorage, configuration: CompilerConfiguration) {
            with(storage) {
                FirExtensionRegistrarAdapter.registerExtension(MinimalFirExtensionRegistrar())
            }
        }
    }
}

class MinimalFirExtensionRegistrar() : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirDeclarationGenerationExtension.Factory(::MinimalFirExtension)
    }
}