@file:Suppress("unused", "ObjectPropertyName")

package love.forte.plugin.suspendtrans.gradle

import BuildConfig.KOTLIN_PLUGIN_ID
import BuildConfig.PLUGIN_VERSION


val org.gradle.plugin.use.PluginDependenciesSpec.`suspend-transform`: org.gradle.plugin.use.PluginDependencySpec
    get() = id(KOTLIN_PLUGIN_ID).version(PLUGIN_VERSION)


