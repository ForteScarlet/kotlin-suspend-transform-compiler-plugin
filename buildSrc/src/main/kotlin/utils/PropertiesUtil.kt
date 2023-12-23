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
 */
fun isMainPublishable(): Boolean = !isCi() || (isCi() && isLinux)
