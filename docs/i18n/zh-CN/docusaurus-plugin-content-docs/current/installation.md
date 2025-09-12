---
id: installation
title: 安装
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { GradlePluginDSL, GradlePluginLegacy } from '@site/src/components/Snippets';
import versionInfo from '@site/src/version.json';

本指南介绍如何在您的项目中安装和设置 Kotlin 挂起函数转换编译器插件。

## 版本信息

- 当前文档构建于的最新版本
  <small>(通常是不包含 `Beta`, `RC` 等的版本)</small>: <br/>
  **v{versionInfo.version}**

- Maven 仓库和 Gradle 插件中的最新版本: <br />
  [![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/)
  [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

- 所有发行版: [前往 GitHub Releases](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/releases)

### 版本命名约定

在 `0.9.0`（包含）之前，版本的命名约定是 `x.y.z`。
但似乎 Kotlin 编译器的内容可能会随着每个版本发生变化，
而这样的版本号不能反映相应的 Kotlin 版本，
因此可能会导致一些混乱。

因此，从 `0.9.0` 之后开始，版本将以 `$Kotlin-$plugin` 的形式命名，
例如 `2.0.20-0.9.1`。
前半部分是用于构建的 Kotlin 版本，后半部分是此插件的版本。

### 版本兼容性表

如果版本小于或等于 `0.9.0`，您可以参考此对照表：

| Kotlin 版本    | 插件版本                    |
|----------------|-------------------------|
| `2.0.0`        | `0.8.0-beta1` ~ `0.9.0` |
| `1.9.22`       | `0.7.0-beta1`           |
| `1.9.21`       | `0.6.0`                 |
| `1.9.10`       | `0.5.1`                 |
| `1.9.0`        | `0.5.0`                 |
| `1.8.21`       | `0.3.1` ~ `0.4.0`       |

:::note
我没有详细记录每个 Kotlin 版本之间编译器插件的兼容性。
根据我的记忆和猜测，当次版本号增加时（例如 `1.8.0` -> `1.9.0`），Kotlin 版本不兼容的概率较高，
而当补丁版本号增加时（例如 `1.9.21` -> `1.9.22`），不兼容的概率较小。
:::

### 版本跟进

通常我们会主动跟进正常的 Kotlin release 版本的更新（例如 `2.1.0`, `2.2.20` 等）。
而对于一些非常规的版本（例如 `2.1.0-RC1`, `2.2.20-Beta2` 等），我们**不保证**每个版本都会跟随着更新。

不过如果：

- 你需要某个版本、这个版本与之前的版本不兼容且我们没有更新
- 某个正常的 release 更新后我们一直没发现/没更新

欢迎随时通过 [issues](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues)
让我们知道～


## Gradle 设置

<Tabs>
  <TabItem value="plugin-dsl" label="Plugins DSL">

**build.gradle.kts**

<GradlePluginDSL></GradlePluginDSL>

  </TabItem>
  <TabItem value="legacy-plugin-application" label="传统插件应用">

**build.gradle.kts**

<GradlePluginLegacy></GradlePluginLegacy>

  </TabItem>
</Tabs>

```kotlin
// 配置它。
suspendTransformPlugin {
    // 配置 SuspendTransformPluginExtension ...
}
```

## 配置

详细的配置说明请参考 [配置](./configuration/configuration.md)。

## 依赖项

插件可能会自动引入一些依赖项。
有关此主题的更多信息，请参考 [配置 - 包含依赖项](./configuration/configuration.md#包含依赖项)。

## 注意事项
### Gradle JVM

**Gradle JVM** 必须是 JDK11+。

### K2 支持

从 `v0.7.0` 开始支持 K2。

### 当前开发项目中的 IDE 支持

IDE **不支持**对当前使用编译器插件的项目进行高亮显示。
如果您想验证编译器插件是否正常工作，您可以：
  
- JVM 平台：
  - 检查编译后的 `.class` 文件或其反编译结果。
  - 在单元测试中使用反射访问预期的函数。
- JS 平台：
  - 通过生成 `.d.ts` 文件检查函数是否正确生成。
  - 在单元测试中使用 `dynamic` 访问预期的函数。
