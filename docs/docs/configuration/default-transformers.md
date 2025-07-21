---
id: default-transformers
title: Default Transformers
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

This guide covers the default transformers provided by the plugin and how to use them effectively.

## Overview

Transformers are the core components that define how suspend functions are transformed for different platforms. 
The plugin provides several built-in transformers that cover the most common use cases.

> **Note**: The default transformers depend on the `annotation` and `runtime` dependencies provided by the plugin. 
> Make sure you include them in your configuration before using default transformers.

## JVM Transformers

### JVM Blocking Transformer

The JVM Blocking transformer generates blocking variants of suspend functions using `runBlocking`.

#### Configuration {#jvm-blocking-configuration}

```kotlin
suspendTransformPlugin {
    transformers {
        // Way 1: Simple addition
        addJvmBlocking()

        // Way 2: Using configuration object
        addJvm(SuspendTransformConfigurations.jvmBlockingTransformer)
        
        // Way 3: Using platform-specific addition
        add(
            TargetPlatform.JVM, 
            SuspendTransformConfigurations.jvmBlockingTransformer
        )
    }
}
```

#### Usage {#jvm-blocking-usage}

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class ApiService {
    @JvmBlocking
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
}
```

  </TabItem>
  <TabItem value="compiled" label="Compiled">

```kotlin
class ApiService {
    @JvmSynthetic
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
    
    @Api4J
    fun fetchDataBlocking(): String = 
        `$runInBlocking$` { fetchData() }
}
```

  </TabItem>
</Tabs>

#### Key Features {#jvm-blocking-key-features}

- **Default Generated Function Suffix**: `Blocking`
- **Return Type**: Same as the original function
- **Runtime Function**: `$runInBlocking$` (based on `kotlinx.coroutines.runBlocking`)

#### Mark Annotation {#jvm-blocking-mark-annotation}

`@JvmBlocking` provides some properties to change the default values and customize the generated function results.

##### baseName {#jvmblocking-basename}

`baseName` represents the base name of the generated function.
By default, it is empty (`""`). If the value is empty, it means using the same value as the original function.

The final function name of the generated function is `baseName` + `suffix`.

##### suffix {#jvmblocking-suffix}

`suffix` represents the suffix of the generated function.
By default, it is `Blocking`.

##### asProperty {#jvmblocking-asproperty}

`asProperty` represents whether to generate a property instead of a function.
By default, it is `false`.

```kotlin
suspend fun foo(): T = ...

// Generated
@Api4J
val fooBlocking: T 
    get() = runInBlocking { foo() }
```

> **Note**: If `asProperty` is `true`, the function cannot have parameters.

##### markName {#jvmblocking-markname}

Refer to [Mark Name](../features/mark-name).


### JVM Async Transformer

The JVM Async transformer generates asynchronous variants using `CompletableFuture`.

#### Configuration {#jvm-async-configuration}

```kotlin
suspendTransformPlugin {
    transformers {
        // Way 1: Simple addition
        addJvmAsync()

        // Way 2: Using configuration object
        addJvm(SuspendTransformConfigurations.jvmAsyncTransformer)
        
        // Way 3: Using platform-specific addition
        add(
            TargetPlatform.JVM, 
            SuspendTransformConfigurations.jvmAsyncTransformer
        )
    }
}
```

#### Usage {#jvm-async-usage}

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class ApiService {
    @JvmAsync
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
}
```

  </TabItem>
  <TabItem value="compiled" label="Compiled">

```kotlin
class ApiService {
    @JvmSynthetic
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
    
    @Api4J
    fun fetchDataAsync(): CompletableFuture<out String> = 
        `$runInAsync$`(
            block = { fetchData() }, 
            scope = this as? CoroutineScope
        )
}
```

  </TabItem>
</Tabs>

#### Key Features {#jvm-async-key-features}

- **Default Generated Function Suffix**: `Async`
- **Return Type**: `CompletableFuture<out T>` where T is the original return type
- **Runtime Function**: `$runInAsync$`
- **Scope Handling**: Uses the current `CoroutineScope` if available, otherwise `GlobalScope`

#### Mark Annotation {#jvm-async-mark-annotation}

`@JvmAsync` provides some properties to change the default values and customize the generated function results.

##### baseName {#jvmasync-basename}

`baseName` represents the base name of the generated function.
By default, it is empty (`""`). If the value is empty, it means using the same value as the original function.

The final function name of the generated function is `baseName` + `suffix`.

##### suffix {#jvmasync-suffix}

`suffix` represents the suffix of the generated function.
By default, it is `Async`.

##### asProperty {#jvmasync-asproperty}

`asProperty` represents whether to generate a property instead of a function.
By default, it is `false`.

```kotlin
suspend fun foo(): T = ...

// Generated
@Api4J
val fooAsync: CompletableFuture<out T> 
    get() = runInAsync { foo() }
```

> **Note**: If `asProperty` is `true`, the function cannot have parameters.

##### markName {#jvmasync-markname}

Refer to [Mark Name](../features/mark-name).

## JavaScript Transformers

### JS Promise Transformer

The JS Promise transformer generates Promise-based variants for JavaScript interoperability.

#### Configuration {#js-promise-configuration}

```kotlin
suspendTransformPlugin {
    transformers {
        // Way 1: Simple addition
        addJsPromise()

        // Way 2: Using configuration object
        addJs(SuspendTransformConfigurations.jsPromiseTransformer)
        
        // Way 3: Using platform-specific addition
        add(
            TargetPlatform.JS, 
            SuspendTransformConfigurations.jsPromiseTransformer
        )
    }
}
```

#### Usage {#js-promise-usage}

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class ApiService {
    @JsPromise
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
}
```

  </TabItem>
  <TabItem value="compiled" label="Compiled">

```kotlin
class ApiService {
    
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
    
    @Api4Js
    fun fetchDataAsync(): Promise<String> = 
        `$runInAsync$`(
            block = { fetchData() }, 
            scope = this as? CoroutineScope
        )
}
```

  </TabItem>
</Tabs>

#### Key Features {#js-promise-key-features}

- **Default Generated Function Suffix**: `Async`
- **Return Type**: `Promise<T>` where T is the original return type
- **Runtime Function**: `$runInAsync$`

#### Mark Annotation {#js-promise-mark-annotation}

`@JsPromise` provides some properties to change the default values and customize the generated function results.

##### baseName {#jspromise-basename}

`baseName` represents the base name of the generated function.
By default, it is empty (`""`). If the value is empty, it means using the same value as the original function.

The final function name of the generated function is `baseName` + `suffix`.

##### suffix {#jspromise-suffix}

`suffix` represents the suffix of the generated function.
By default, it is `Async`.

##### asProperty {#jspromise-asproperty}

`asProperty` represents whether to generate a property instead of a function.
By default, it is `false`.

```kotlin
suspend fun foo(): T = ...

// Generated
@Api4Js
val fooAsync: Promise<T> 
    get() = runInAsync { foo() }
```

> **Note**: If `asProperty` is `true`, the function cannot have parameters.

##### markName {#jspromise-markname}

Refer to [Mark Name](../features/mark-name).

## Convenience Functions

### Combined JVM Transformers

```kotlin
suspendTransformPlugin {
    transformers {
        // Includes both addJvmBlocking() and addJvmAsync()
        useJvmDefault()
    }
}
```

This is equivalent to:

```kotlin
suspendTransformPlugin {
    transformers {
        addJvmBlocking()
        addJvmAsync()
    }
}
```

### Combined JS Transformers

```kotlin
suspendTransformPlugin {
    transformers {
        // Includes addJsPromise()
        useJsDefault()
    }
}
```

### All Default Transformers

```kotlin
suspendTransformPlugin {
    transformers {
        // Includes all default transformers for all platforms
        useDefault()
    }
}
```

This is equivalent to:

```kotlin
suspendTransformPlugin {
    transformers {
        useJvmDefault()  // JVM Blocking + JVM Async
        useJsDefault()   // JS Promise
    }
}
```

## Combining Multiple Annotations

You can use multiple transformer annotations on the same function:

```kotlin
class ApiService {
    @JvmBlocking
    @JvmAsync
    @JsPromise
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
}
```

This will generate:
- `fetchDataBlocking(): String` (JVM)
- `fetchDataAsync(): CompletableFuture<out String>` (JVM)
- `fetchDataAsync(): Promise<String>` (JS)
