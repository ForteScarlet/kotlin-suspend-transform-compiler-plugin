# JsExport Integration

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

