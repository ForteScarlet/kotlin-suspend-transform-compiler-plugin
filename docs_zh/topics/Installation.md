# 安装

本指南介绍如何在您的项目中安装和设置 Kotlin 挂起函数转换编译器插件。

## 版本信息

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

> **注意**：我没有详细记录每个 Kotlin 版本之间编译器插件的兼容性。
> 根据我的记忆和猜测，当次版本号增加时（例如 `1.8.0` -> `1.9.0`），Kotlin 版本不兼容的概率较高，
> 而当补丁版本号增加时（例如 `1.9.21` -> `1.9.22`），不兼容的概率较小。

## Gradle 设置

<tabs group="gradle-setup">
<tab id="plugin-dsl" title="Plugins DSL">

**build.gradle.kts**

<include from="snippets.topic" origin="docs" element-id="gradle-plugin-dsl"></include>

</tab>
<tab id="legacy-plugin-application" title="传统插件应用">

**build.gradle.kts**

<include from="snippets.topic" origin="docs" element-id="gradle-plugin-legacy"></include>

</tab>
</tabs>

```kotlin
// 配置它。
suspendTransformPlugin {
    // 配置 SuspendTransformPluginExtension ...
}
```

## 依赖项

插件可能会自动引入一些依赖项。
有关此主题的更多信息，请参考 [](Configuration.md#包含依赖项)。

## 注意事项
### Gradle JVM

**Gradle JVM** 必须是 JDK11+。

### K2 支持

从 `v0.7.0` 开始支持 K2。

### 当前开发项目中的 IDE 支持

IDE 不支持对当前使用编译器插件的项目进行高亮显示。
如果您想验证编译器插件是否正常工作，您可以：
  
- JVM 平台：
  - 检查编译后的类文件或其反编译结果。
  - 在单元测试中使用反射访问预期的函数。
- JS 平台：
  - 通过生成 `.d.ts` 文件检查函数是否正确生成。
  - 在单元测试中使用 `dynamic` 访问预期的函数。