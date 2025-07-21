---
id: jsexport-integration
title: JsExport 集成
---

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
