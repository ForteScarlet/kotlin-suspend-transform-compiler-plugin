package com.example.plugin

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

/**
 * Registrar for the minimal compiler plugin that reproduces the issue with generic annotations.
 */
class MinimalCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        // Register our FIR extension
        FirExtensionRegistrarAdapter.registerExtension(MinimalFirExtensionRegistrar())
    }
}

/**
 * FIR extension registrar that registers our MinimalFirExtension.
 */
class MinimalFirExtensionRegistrar : FirExtensionRegistrarAdapter() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::MinimalFirExtension
    }
}
