/*
 * Copyright (c) 2022-2025 Forte Scarlet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import love.forte.plugin.suspendtrans.gradle.SuspendTransPluginConstants

@Deprecated(
    "Use SuspendTransPluginConstants",
    ReplaceWith(
        "SuspendTransPluginConstants",
        imports = ["love.forte.plugin.suspendtrans.gradle.SuspendTransPluginConstants"]
    )
)
internal object BuildConfig {
    internal const val KOTLIN_PLUGIN_ID: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_ID

    internal const val PLUGIN_VERSION: String = SuspendTransPluginConstants.PLUGIN_VERSION

    internal const val KOTLIN_PLUGIN_GROUP: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_GROUP

    internal const val KOTLIN_PLUGIN_NAME: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_NAME

    internal const val KOTLIN_PLUGIN_VERSION: String = SuspendTransPluginConstants.KOTLIN_PLUGIN_VERSION

    internal const val ANNOTATION_GROUP: String = SuspendTransPluginConstants.ANNOTATION_GROUP

    internal const val ANNOTATION_NAME: String = SuspendTransPluginConstants.ANNOTATION_NAME

    internal const val ANNOTATION_VERSION: String = SuspendTransPluginConstants.ANNOTATION_VERSION

    internal const val RUNTIME_GROUP: String = SuspendTransPluginConstants.RUNTIME_GROUP

    internal const val RUNTIME_NAME: String = SuspendTransPluginConstants.RUNTIME_NAME

    internal const val RUNTIME_VERSION: String = SuspendTransPluginConstants.RUNTIME_VERSION
}
