---
title: 配置
sidebar_position: 1
---

本指南介绍如何配置 Kotlin 挂起函数转换编译器插件扩展。

## 基本配置

### 启用

启用或禁用 Kotlin 编译器插件。
默认值为 `true`。

```kotlin
suspendTransformPlugin {
    enabled = true
}
```

### 包含依赖项

如果您希望使用我们提供的转换器，那么您可能需要添加 `annotation` 和 `runtime` 依赖项。

您可以通过配置自动添加它们。

```kotlin
suspendTransformPlugin {
    // 包含注解
    // 默认为 `true`
    includeAnnotation = true
    // 默认可以不配置，专门使用默认值。
    annotationDependency {
        // 默认为 `compileOnly`
        configurationName = "compileOnly"
        // 默认与插件版本相同
        version = "<ANNOTATION_VERSION>"
    }

    // 包含运行时
    // 默认为 `true`
    includeRuntime = true
    // 默认可以不配置，专门使用默认值。
    runtimeDependency {
        // 默认为 `implementation`
        configurationName = "implementation"
        // 默认与插件版本相同
        version = "<RUNTIME_VERSION>"
    }
}
```

您也可以禁用它们并手动添加依赖项。

```kotlin
plugins {
    kotlin("jvm") version "..." // 以 Kotlin/JVM 为例
    id("love.forte.plugin.suspend-transform") version "<VERSION>"
}

dependencies {
    // 注解
    compileOnly("love.forte.plugin.suspend-transform:suspend-transform-annotation:<VERSION>")
    // 运行时
    implementation("love.forte.plugin.suspend-transform:suspend-transform-runtime:<VERSION>")
}

suspendTransformPlugin {
    // 禁用它们
    includeAnnotation = false
    includeRuntime = false
}
```

## 转换器配置

`Transformer` 是用于描述挂起函数如何转换的类型。
您需要添加一些 `Transformer` 来使编译器插件实际工作。

### 基本转换器设置

```kotlin
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations

suspendTransformPlugin {
    // 配置转换器
    transformers {
        add(TargetPlatform.JVM) { // this: TransformerSpec
            // 配置 TransformerSpec...
        }

        addJvm { // this: TransformerSpec
            // 配置 TransformerSpec...
        }

        // 使用我们从 `SuspendTransformConfigurations` 提供的默认转换器
        add(TargetPlatform.JVM, jvmBlockingTransformer)

        addJvm { // this: TransformerSpec
            // 从一个 Transformer 修改和调整
            from(jvmBlockingTransformer)
            // 进一步配置...
        }
    }
}
```

:::info
`love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations`
包含一些我们提供的标准实现的信息。
:::


### 使用默认转换器

首先，我们提供一些简单且常用的实现。
您可以通过配置简单快速地使用它们。

有关使用默认转换器的信息，
请参考 [默认转换器](./default-transformers.md)。

:::note
默认的 `Transformer` 依赖于我们提供的 `annotation` 和 `runtime`。
在使用之前请确保您包含了它们。
:::


## 完整配置示例

这是一个显示各种配置选项的完整示例：

```kotlin
suspendTransformPlugin {
    // 启用插件
    enabled = true

    // 包含默认依赖项
    includeAnnotation = true
    includeRuntime = true

    // 配置转换器
    transformers {
        // 使用所有默认转换器
        useDefault()

        // 或者单独配置：
        // addJvmBlocking()
        // addJvmAsync()
        // addJsPromise()
    }
}
```
