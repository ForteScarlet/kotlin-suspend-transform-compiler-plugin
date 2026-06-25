---
id: default-transformers
title: Default Transformers
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { VersionBadge } from '@site/src/components/Snippets';

This guide covers the default transformers provided by the plugin and how to use them effectively.

## Overview

Transformers are the core components that define how suspend functions are transformed for different platforms. 
The plugin provides several built-in transformers that cover the most common use cases.

:::note
The default transformers depend on the `annotation` and `runtime` dependencies provided by the plugin.
Make sure you include them in your configuration before using default transformers.
:::


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

#### Lifecycle And Dispatcher {#jvm-blocking-lifecycle}

The generated blocking bridge is only intended for Java-style blocking
interop. It blocks the calling thread until the suspend function completes.
Thread interruption cancels the bridge and is reported as `InterruptedException`.

The default runtime uses `runBlocking(Dispatchers.IO)`. This keeps the suspend
body off the calling thread after dispatch and is a conservative default for
possibly blocking work, but the caller is still blocked. Avoid generated
blocking bridges from coroutines, UI/event-loop threads, or other thread-limited
execution paths.

Use a custom transformer/runtime when a specific dispatcher, transaction
context, MDC, or thread affinity is required.

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

:::note
If `asProperty` is `true`, the function cannot have parameters.
:::


##### markName {#jvmblocking-markname}

Refer to [Mark Name](../features/mark-name.md).


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

#### Lifecycle And Cancellation {#jvm-async-lifecycle}

If the receiver is a `CoroutineScope`, the generated bridge launches the
coroutine in that scope. Otherwise, the default runtime uses `GlobalScope`.

Cancelling the returned `CompletableFuture` cancels the coroutine. Dropping a
long-running future without cancelling it does not stop the underlying work, so
Java callers should keep and cancel the returned future when the operation is no
longer needed.

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

:::note
If `asProperty` is `true`, the function cannot have parameters.
:::


##### markName {#jvmasync-markname}

Refer to [Mark Name](../features/mark-name.md).

### JVM Reactive Transformer

<VersionBadge version="0.14.0" />

The JVM Reactive transformer generates Reactive Streams `Publisher` variants.

#### Configuration {#jvm-reactive-configuration}

```kotlin
suspendTransformPlugin {
    transformers {
        // Way 1: Simple addition
        addJvmReactive()

        // Way 2: Using configuration object
        addJvm(SuspendTransformConfigurations.jvmReactiveTransformer)
    }
}
```

#### Usage {#jvm-reactive-usage}

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
@OptIn(ExperimentalJvmApi::class)
class ApiService {
    @JvmReactive
    suspend fun fetchData(): String? = null
}
```

  </TabItem>
  <TabItem value="compiled" label="Compiled">

```kotlin
class ApiService {
    @JvmSynthetic
    suspend fun fetchData(): String? = null

    @Api4J
    fun fetchDataReactive(): Publisher<String> =
        `$runInReactive$`(
            block = { fetchData() },
            scope = this as? CoroutineScope
        )
}
```

  </TabItem>
</Tabs>

#### Key Features {#jvm-reactive-key-features}

- **Default Generated Function Suffix**: `Reactive`
- **Return Type**: `Publisher<T & Any>` where T is the original return type
- **Runtime Function**: `$runInReactive$`
- **Null Handling**: `null` results complete empty
- **Scope Handling**: the generated call may pass a `CoroutineScope`, but the
  default runtime keeps the parameter only for generated bridge compatibility
  and future runtime strategies. It does not use it as a coroutine parent or
  dispatcher source.

#### Lifecycle And Cancellation {#jvm-reactive-lifecycle}

The returned `Publisher` is cold: the suspend block starts when a subscriber
subscribes. Cancelling the Reactive Streams `Subscription` cancels the publisher
coroutine.

Reactive scheduling is not configured by the default bridge. Use the caller's
reactive chain, `withContext` inside the suspend function, or a custom
transformer/runtime for dispatcher or lifecycle requirements.

:::note
This transformer requires
[`org.reactivestreams.Publisher`](https://www.reactive-streams.org/) and
[`org.jetbrains.kotlinx:kotlinx-coroutines-reactive`](https://github.com/Kotlin/kotlinx.coroutines/blob/master/reactive/README.md)
on the JVM classpath. These dependencies are not added automatically by
`addJvmReactive()`; add them to the user JVM project or source set.
:::

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

:::note
If `asProperty` is `true`, the function cannot have parameters.
:::


##### markName {#jspromise-markname}

Refer to [Mark Name](../features/mark-name.md).

## Convenience Functions

### Combined JVM Transformers

```kotlin
suspendTransformPlugin {
    transformers {
        // Includes addJvmBlocking() and addJvmAsync()
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

`addJvmReactive()` is not included in `useJvmDefault()` and must be enabled explicitly.

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
    @JvmReactive
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
- `fetchDataReactive(): Publisher<String>` (JVM)
- `fetchDataAsync(): Promise<String>` (JS)
