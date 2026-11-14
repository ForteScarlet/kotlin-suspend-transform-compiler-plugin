# Local sample tests

Run commands from the repository root. Use the Gradle version configured in `gradle/wrapper/gradle-wrapper.properties` with a compatible JDK. For example, Gradle 9.5.0 runs on JDK 17–26; choose a version compatible with your Gradle and Kotlin versions using the [Gradle compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) and [Kotlin Gradle compatibility table](https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin).

The JVM and KMP samples currently specify a JDK 11 toolchain in their build scripts. Install JDK 11 and let Gradle discover it, or configure its location as described below. The toolchain can be separate from the JDK running Gradle.

Set `GRADLE` to the wrapper when its JAR is available, or to a compatible Gradle installation:

```sh
GRADLE=./gradlew
# Alternatively: GRADLE=/path/to/gradle/bin/gradle
```

Set `JAVA_HOME` if needed to select the JDK running Gradle. If the wrapper JAR is missing, use an installed distribution matching the wrapper version.

## 1. Publish locally

Keep the sample modules commented out in `settings.gradle.kts` while publishing:

```sh
"$GRADLE" publishToMavenLocal
```

Publication creates empty `-javadoc.jar` files. To generate API documentation separately:

```sh
"$GRADLE" :plugins:suspend-transform-plugin-gradle:dokkaGeneratePublicationHtml
```

HTML output is under the module's `build/dokka/html/` directory.

The plugin version combines the Kotlin version and `IProject.pluginVersion`, for example `2.5.0-RC-0.15.0`. Keep `IProject.ktVersion` and `gradle/libs.versions.toml` consistent.

## 2. Enable the samples

In `settings.gradle.kts`, put `mavenLocal()` first in both `pluginManagement.repositories` and `dependencyResolutionManagement.repositories`, keeping the existing repositories. Uncomment:

```kotlin
include(":tests:test-jvm")
include(":tests:test-js")
include(":tests:test-kmp")
```

Set `tests/build.gradle.kts` to the locally published version:

```kotlin
plugins {
    id("love.forte.plugin.suspend-transform") version "2.5.0-RC-0.15.0" apply false
}
```

The three sample build scripts already declare `mavenLocal()`. Their runtime and annotation dependencies use project dependencies; the Gradle plugin and embeddable compiler plugin resolve from Maven Local.

The Android sample currently references an absent `waltid-crypto` project and has the suspend-transform plugin disabled. Leave it commented out.

## 3. Compile and test

```sh
"$GRADLE" kotlinUpgradeYarnLock
"$GRADLE" \
  :tests:test-jvm:test \
  :tests:test-kmp:jvmTest \
  :tests:test-js:jsNodeTest \
  :tests:test-js:jsDevelopmentExecutableCompileSync \
  :tests:test-kmp:jsDevelopmentExecutableCompileSync
```

If Gradle cannot find the JDK 11 toolchain, add `-Porg.gradle.java.installations.paths=/path/to/jdk-11` to the test command.

Update the Yarn lock when enabling the samples changes its contents. Restore that temporary change after testing. After republishing the same version, add `--rerun-tasks` if Gradle reuses sample compilation or test results.

Reports are under each module's `build/reports/tests/`: `test`, `jvmTest`, and `jsNodeTest`, respectively.

## 4. Inspect generated code

```sh
javap -p -c -s -classpath tests/test-jvm/build/classes/kotlin/main \
  love.forte.plugin.suspendtrans.sample.ForteScarlet \
  love.forte.plugin.suspendtrans.sample.JvmReactiveSamples
javap -p -c -s -classpath tests/test-kmp/build/classes/kotlin/jvm/main \
  example.MyClass
```

You can also open the compiled `.class` files in IntelliJ IDEA to inspect their decompiled Kotlin declarations.

Check the Blocking, Async, and Reactive signatures and their calls to `RunInSuspendJvmKt`. Check nullable generic arguments through the reflection tests; JVM descriptors erase nullability.

Inspect JS files under `tests/test-js/build/compileSync/js/main/developmentExecutable/`: `kotlin/` contains `.mjs` implementations, and `typescript-definitions/` contains `.d.mts` declarations. Check `stringToIntAsync`, its runtime call, and the `Promise` return type. The KMP sample uses the same output layout.

The published embeddable JAR must contain these entries at its root:

```text
META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
META-INF/services/org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
```

Use `jar tf` to check it. Dependencies must be unpacked; nested dependency JARs prevent plugin discovery.

## 5. Restore temporary configuration

Comment out the three sample includes again. Restore temporary repository changes, the sample plugin version, and `kotlin-js-store/yarn.lock` from their pre-test state. Keep compiler or packaging fixes found during testing. Review `git diff` before finishing.
