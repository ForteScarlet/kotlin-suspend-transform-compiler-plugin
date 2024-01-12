@file:Suppress("unused", "ObjectPropertyName")

package love.forte.plugin.suspendtrans.gradle

import love.forte.plugin.suspendtrans.gradle.SuspendTransPluginConstants.KOTLIN_PLUGIN_ID
import love.forte.plugin.suspendtrans.gradle.SuspendTransPluginConstants.PLUGIN_VERSION


val org.gradle.plugin.use.PluginDependenciesSpec.`suspend-transform`: org.gradle.plugin.use.PluginDependencySpec
    get() = id(KOTLIN_PLUGIN_ID).version(PLUGIN_VERSION)


