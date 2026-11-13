# 本地示例测试

在仓库根目录执行命令。使用 `gradle/wrapper/gradle-wrapper.properties` 中配置的 Gradle 版本和与之兼容的 JDK。例如，Gradle 9.5.0 可以运行在 JDK 17–26 上；选择版本时可参考 [Gradle 兼容性表](https://docs.gradle.org/current/userguide/compatibility.html)和 [Kotlin Gradle 兼容性表](https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin)。

JVM 和 KMP 示例目前在构建脚本中指定了 JDK 11 工具链。安装 JDK 11 后，可以让 Gradle 自动查找，或按下文设置其路径。工具链可以与运行 Gradle 的 JDK 分开。

将 `GRADLE` 指向包含 wrapper JAR 的 wrapper，或兼容的 Gradle 安装：

```sh
GRADLE=./gradlew
# 或者：GRADLE=/path/to/gradle/bin/gradle
```

如有需要，通过 `JAVA_HOME` 选择运行 Gradle 的 JDK。如果缺少 wrapper JAR，可以使用与 wrapper 版本一致的已安装发行版。

## 1. 发布到本地

发布时保持 `settings.gradle.kts` 中的示例模块处于注释状态：

```sh
"$GRADLE" publishToMavenLocal
```

发布时生成空的 `-javadoc.jar`。单独生成 API 文档：

```sh
"$GRADLE" :plugins:suspend-transform-plugin-gradle:dokkaGeneratePublicationHtml
```

HTML 输出位于模块的 `build/dokka/html/` 目录。

插件版本由 Kotlin 版本与 `IProject.pluginVersion` 组合，例如 `2.5.0-RC-0.15.0`。保持 `IProject.ktVersion` 与 `gradle/libs.versions.toml` 一致。

## 2. 启用示例

在 `settings.gradle.kts` 的 `pluginManagement.repositories` 和 `dependencyResolutionManagement.repositories` 中，将 `mavenLocal()` 放在首位，保留原有仓库。取消以下注释：

```kotlin
include(":tests:test-jvm")
include(":tests:test-js")
include(":tests:test-kmp")
```

将 `tests/build.gradle.kts` 设置为本地发布的版本：

```kotlin
plugins {
    id("love.forte.plugin.suspend-transform") version "2.5.0-RC-0.15.0" apply false
}
```

三个示例的构建脚本已经声明了 `mavenLocal()`。它们的 runtime 和 annotation 使用项目依赖，Gradle 插件和 embeddable 编译器插件从 Maven Local 解析。

Android 示例目前引用了不存在的 `waltid-crypto` 项目，且未启用 suspend-transform 插件，保持注释即可。

## 3. 编译和测试

```sh
"$GRADLE" kotlinUpgradeYarnLock
"$GRADLE" \
  :tests:test-jvm:test \
  :tests:test-kmp:jvmTest \
  :tests:test-js:jsNodeTest \
  :tests:test-js:jsDevelopmentExecutableCompileSync \
  :tests:test-kmp:jsDevelopmentExecutableCompileSync
```

如果 Gradle 找不到 JDK 11 工具链，在测试命令中添加 `-Porg.gradle.java.installations.paths=/path/to/jdk-11`。

启用示例导致 Yarn lock 内容变化时，更新 lock。测试后恢复这项临时修改。同版本重新发布后，如果 Gradle 复用了示例编译或测试结果，添加 `--rerun-tasks`。

报告位于各模块的 `build/reports/tests/` 下，分别为 `test`、`jvmTest` 和 `jsNodeTest`。

## 4. 检查生成代码

```sh
javap -p -c -s -classpath tests/test-jvm/build/classes/kotlin/main \
  love.forte.plugin.suspendtrans.sample.ForteScarlet \
  love.forte.plugin.suspendtrans.sample.JvmReactiveSamples
javap -p -c -s -classpath tests/test-kmp/build/classes/kotlin/jvm/main \
  example.MyClass
```

也可以在 IntelliJ IDEA 中打开编译后的 `.class` 文件，查看反编译的 Kotlin 声明。

检查 Blocking、Async、Reactive 方法签名及其对 `RunInSuspendJvmKt` 的调用。泛型参数的可空性通过反射测试检查，JVM 描述符会擦除可空性。

检查 `tests/test-js/build/compileSync/js/main/developmentExecutable/` 下的 JS 文件：`kotlin/` 包含 `.mjs` 实现，`typescript-definitions/` 包含 `.d.mts` 声明。检查 `stringToIntAsync`、对应 runtime 调用及 `Promise` 返回类型。KMP 示例使用相同的输出目录结构。

发布的 embeddable JAR 根目录必须包含以下条目：

```text
META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
META-INF/services/org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
```

使用 `jar tf` 检查。依赖需要解包，嵌套依赖 JAR 会导致插件无法被发现。

## 5. 恢复临时配置

重新注释三个示例的 include。将临时仓库配置、示例插件版本和 `kotlin-js-store/yarn.lock` 恢复到测试前的状态。保留测试中发现的编译器或打包修复。结束前检查 `git diff`。
