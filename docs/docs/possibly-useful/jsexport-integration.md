---
id: jsexport-integration
title: JsExport Integration
sidebar_position: 1
---

:::caution

Starting with version 2.3.0, Kotlin’s `@JsExport` supports exporting suspend functions (see [What's new in Kotlin 2.3.0](https://kotlinlang.org/docs/whatsnew23.html#new-export-of-suspend-function-with-jsexport)), and starting with version 2.4.20, it supports exporting suspend lambdas (see [What's new in Kotlin 2.4.20](https://kotlinlang.org/docs/whatsnew2420.html#new-export-of-suspend-lambda-with-jsexport)).

Therefore, on the JS platform, you can choose to use the native `@JsExport` to meet your export needs, without having to use this compiler plugin or perform any additional configuration. The official features are always the safer choice.

:::

If you want to use `@JsExport` with the default JS configuration, 
you need special handling to prevent conflicts:

```kotlin
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations

plugins {
    // ...
}

suspendTransformPlugin {
    transformers {
        addJsPromise {
            addCopyAnnotationExclude {
                // The generated function does not include (copy) `@JsExport.Ignore`.
                from(kotlinJsExportIgnoreClassInfo)
            }
        }
    }
}
```

## Usage Example

```kotlin
@file:OptIn(ExperimentalJsExport::class)

@JsExport
class Foo {
    @JsPromise
    @JsExport.Ignore
    suspend fun run(): Int = ...
}
```

This configuration ensures that:
- The original suspend function is marked with `@JsExport.Ignore`
- The generated Promise-based function is exported to JavaScript
- No conflicts occur between the suspend function and the Promise variant
