package utils

import org.gradle.api.provider.Property

fun systemProperty(propKey: String, envKey: String = propKey): String? {
    return System.getProperty(propKey, System.getenv(envKey))
}

infix fun <T> Property<T>.by(value: T) {
    set(value)
}
