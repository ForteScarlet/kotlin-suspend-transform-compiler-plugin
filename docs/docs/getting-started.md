---
id: getting-started
title: Getting Started
sidebar_position: 3
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { Badge } from '@site/src/components/Snippets';

This guide will walk you through the basic usage of the Kotlin Suspend Transform Compiler Plugin across different platforms.

## Configuration

First, enable the default configuration by configuring the Gradle plugin. 

:::note

For more information on configuration, refer to [Configuration](./configuration).

:::

**build.gradle.kts**

```kotlin
suspendTransformPlugin {
    // `enable = true` is default.
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

## JVM Platform

The JVM platform supports both blocking and asynchronous transformations of suspend functions.

### Basic Usage {#jvm-basic-usage}

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class Foo {
    @JvmBlocking
    @JvmAsync
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}
```

  </TabItem>
  <TabItem value="compiled" label="Compiled">

```kotlin
class Foo {
    // Hide from Java
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    
    @Api4J // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetBlocking(): String = runInBlocking { waitAndGet() } // 'runInBlocking' from the runtime provided by the plugin

    @Api4J // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetAsync(): CompletableFuture<out String> = runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
}
```

  </TabItem>
</Tabs>

## JavaScript Platform

The JavaScript platform supports Promise-based transformations.

### Basic Usage {#js-basic-usage}

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}
```

  </TabItem>
  <TabItem value="compiled" label="Compiled">

```kotlin
class Foo {
    
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    
    @Api4Js // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetAsync(): Promise<String> = runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
}
```

  </TabItem>
</Tabs>

> **Note**: JS platform support was added in version 0.6.0! See the development process at [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993), and the final implementation at [#39](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/39)!

## WasmJS Platform

<Badge type="primary">Experimental</Badge>
<Badge type="secondary">Version 0.6.0</Badge>

:::warning
The transformer functions mentioned in the WasmJS example below 
are assumed to be custom functions defined by YOU.

They are not included in the `runtime`. 

Since there are a lot of restrictions on the use of various types in WasmJS...
so I'm not sure how to handle them perfectly yet.
Until then, you can customize functions and types to control the behaviour of the compiler plugin yourself.
Just like you can customize other platforms.
:::

```kotlin
// Some custom transformer functions by YOU...
fun <T> runInAsync(block: suspend () -> T): AsyncResult<T> = AsyncResult(block)

class AsyncResult<T>(val block: suspend () -> T) {
    @OptIn(DelicateCoroutinesApi::class)
    fun toPromise(): Promise<JsAny?> {
        return GlobalScope.promise { block() }
    }
}
```

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
}
```

  </TabItem>
  <TabItem value="compiled" label="Compiled">

```kotlin
class Foo {
    
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    
    @Api4Js // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetAsync(): AsyncResult<String> = runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
}
```

  </TabItem>
</Tabs>
