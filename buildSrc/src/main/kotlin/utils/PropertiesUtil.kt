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

package utils

import org.gradle.api.provider.Property
import java.time.Duration
import java.util.concurrent.TimeUnit

fun systemProperty(propKey: String, envKey: String = propKey): String? {
    return System.getProperty(propKey, System.getenv(envKey))
}

infix fun <T> Property<T>.by(value: T) {
    set(value)
}

infix fun Property<Duration>.by(value: Long): DurationHolder = DurationHolder(this, value)

data class DurationHolder(private val property: Property<Duration>, private val time: Long) {
    infix fun unit(unit: TimeUnit) {
        if (unit == TimeUnit.SECONDS) {
            property.set(Duration.ofSeconds(time))
        } else {
            property.set(Duration.ofMillis(unit.toMillis(time)))
        }
    }
}

/**
 * 是否发布 release
 */
fun isRelease(): Boolean = systemProperty("RELEASE").toBoolean()

/**
 * 是否在CI中
 */
fun isCi(): Boolean = systemProperty("CI").toBoolean()

/**
 * 是否自动配置gradle的发布
 */
fun isAutomatedGradlePluginPublishing(): Boolean = isCi() && systemProperty("PLUGIN_AUTO").toBoolean()

val isLinux: Boolean = systemProperty("os.name")?.contains("linux", true) ?: false

/**
 * 如果在 CI 中，则必须是 linux 平台才运作。
 * 如果不在 CI 中，始终运作。
 *
 * 多平台发布已经不再需要分多个系统环境了，
 * 不再需要判断 Main Host 了。
 */
fun isMainPublishable(): Boolean = true // !isCi() || (isCi() && isLinux)
