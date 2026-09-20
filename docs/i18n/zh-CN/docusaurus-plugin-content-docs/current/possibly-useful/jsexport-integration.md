---
id: jsexport-integration
title: JsExport 集成
sidebar_position: 1
---

:::caution

Kotlin 的 [@JsExport](https://kotlinlang.org/docs/js-to-kotlin-interop.html#jsexport-annotation)
自 2.3.0 开始支持对 suspend 函数的导出（参考 [What's new in Kotlin 2.3.0](https://kotlinlang.org/docs/whatsnew23.html#new-export-of-suspend-function-with-jsexport)）、自 2.4.20 开始支持对 suspend Lambda 的导出（参考 [What's new in Kotlin 2.4.20](https://kotlinlang.org/docs/whatsnew2420.html#new-export-of-suspend-lambda-with-jsexport)）。

因此，在 JS 平台上你可以选择使用原生的 `@JsExport` 来满足你的导出需求，而不再需要使用此编译器插件或进行什么额外配置。
官方提供的能力总是更加妥善的选择。

:::

如果您想将 `@JsExport` 与默认 JS 配置一起使用，
您需要特殊处理以防止冲突：

```kotlin
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations

plugins {
    // ...
}

suspendTransformPlugin {
    transformers {
        addJsPromise {
            addCopyAnnotationExclude {
                // 生成的函数不包含（复制）`@JsExport.Ignore`。
                from(kotlinJsExportIgnoreClassInfo)
            }
        }
    }
}
```

## 使用示例

```kotlin
@file:OptIn(ExperimentalJsExport::class)

@JsExport
class Foo {
    @JsPromise
    @JsExport.Ignore
    suspend fun run(): Int = ...
}
```

此配置确保：
- 原挂起函数标记为 `@JsExport.Ignore`
- 生成的基于 Promise 的函数导出到 JavaScript
- 挂起函数和 Promise 变体之间不会发生冲突
