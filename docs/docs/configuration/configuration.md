---
title: Configuration
sidebar_position: 1
---

This guide covers how to configure the Kotlin Suspend Transform Compiler Plugin extension.

## Basic Configuration

### Enabled

Enable or disable the Kotlin compiler plugin.
Default value is `true`.

```kotlin
suspendTransformPlugin {
    enabled = true
}
```

### Include Dependencies

If you wish to use the transformers we provide, then you may need to add the `annotation` and `runtime` dependencies.

You can add them automatically via configuration.

```kotlin
suspendTransformPlugin {
    // include the annotation
    // Default is `true`
    includeAnnotation = true
    // The default can be left unconfigured and the default values are used exclusively.
    annotationDependency {
        // Default is `compileOnly`
        configurationName = "compileOnly"
        // Default is same as the plugin version
        version = "<ANNOTATION_VERSION>"
    }

    // Include the runtime
    // Default is `true`
    includeRuntime = true
    // The default can be left unconfigured and the default values are used exclusively.
    runtimeDependency {
        // Default is `implementation`
        configurationName = "implementation"
        // Default is same as the plugin version
        version = "<RUNTIME_VERSION>"
    }
}
```

You can also disable them and add dependencies manually.

```kotlin
plugins {
    kotlin("jvm") version "..." // Take the Kotlin/JVM as an example
    id("love.forte.plugin.suspend-transform") version "<VERSION>"
}

dependencies {
    // annotation
    compileOnly("love.forte.plugin.suspend-transform:suspend-transform-annotation:<VERSION>")
    // runtime
    implementation("love.forte.plugin.suspend-transform:suspend-transform-runtime:<VERSION>")
}

suspendTransformPlugin {
    // Disable them
    includeAnnotation = false
    includeRuntime = false
}
```

## Transformer Configuration

`Transformer` is the type used to describe how the suspend function is transformed.
You need to add some `Transformer`s to make the compiler plugin actually work.

### Basic Transformer Setup

```kotlin
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations

suspendTransformPlugin {
    // Config the transformers
    transformers {
        add(TargetPlatform.JVM) { // this: TransformerSpec
            // Config the TransformerSpec...
        }

        addJvm { // this: TransformerSpec
            // Config the TransformerSpec...
        }

        // Use a default transformer we provided from `SuspendTransformConfigurations`
        add(TargetPlatform.JVM, jvmBlockingTransformer)

        addJvm { // this: TransformerSpec
            // Modify and adjust from a Transformer
            from(jvmBlockingTransformer)
            // Further configurations...
        }
    }
}
```

> `love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations`
> contains some information about the standard implementations we provide.

### Using Default Transformers

First, we provide some simple and commonly used implementations.
You can use them simply and quickly through configuration.

For information on using the default transformers, 
refer to [Default Transformers](default-transformers.md).

> **Note**: The default `Transformer`s depend on the `annotation` and `runtime` we provide.
> Make sure you include them before using it.

## Complete Configuration Example

Here’s a complete example showing various configuration options:

```kotlin
suspendTransformPlugin {
    // Enable the plugin
    enabled = true

    // Include default dependencies
    includeAnnotation = true
    includeRuntime = true

    // Configure transformers
    transformers {
        // Use all default transformers
        useDefault()

        // Or configure individually:
        // addJvmBlocking()
        // addJvmAsync()
        // addJsPromise()
    }
}
```
