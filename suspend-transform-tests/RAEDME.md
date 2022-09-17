
1. Publish to maven local:

```shell
gradle clean publishToMavenLocal
```

2. Edit the `setting.gradle.kts`:

_Restore commented out test content._

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("love.forte.plugin.suspend-transform") version "0.0.1"
    }
}

rootProject.name = "kotlin-suspend-transform-compiler-plugin"

include(":suspend-transform-runtime")
include(":suspend-transform-plugin")
include(":suspend-transform-plugin-gradle")
include(":suspend-transform-tests:suspend-transform-test-jvm")
include(":suspend-transform-tests:suspend-transform-test-js")
```

3. Run. [JS Test](suspend-transform-test-js/src/main/kotlin/Main.kt) or [JVM Test](suspend-transform-test-jvm/src/main/kotlin/Main.kt)