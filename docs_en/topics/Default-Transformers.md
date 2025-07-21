# Default Transformers

This guide covers the default transformers provided by the plugin and how to use them effectively.

## Overview

Transformers are the core components that define how suspend functions are transformed for different platforms. 
The plugin provides several built-in transformers that cover the most common use cases.

> **Note**: The default transformers depend on the `annotation` and `runtime` dependencies provided by the plugin. 
> Make sure you include them in your configuration before using default transformers.

## JVM Transformers

### JVM Blocking Transformer

The JVM Blocking transformer generates blocking variants of suspend functions using `runBlocking`.

#### Configuration {id=jvm-blocking-configuration}

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

#### Usage {id=jvm-blocking-usage}

<compare type="top-bottom" first-title="Source" second-title="Compiled" xmlns="">

```kotlin
class ApiService {
    @JvmBlocking
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
}
```

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

</compare>

#### Key Features {id=jvm-blocking-key-features}

- **Default Generated Function Suffix**: `Blocking`
- **Return Type**: Same as the original function
- **Runtime Function**: `$runInBlocking$` (based on `kotlinx.coroutines.runBlocking`)

#### Mark Annotation {id="jvm-blocking-mark-annotation"}

`@JvmBlocking` provides some properties to change the default values and customize the generated function results.

<deflist>
<def title="baseName" id="@JvmBlocking-baseName">

`baseName` represents the base name of the generated function.
By default, it is empty (`""`). If the value is empty, it means using the same value as the original function.

The final function name of the generated function is `baseName` + `suffix`.

</def>
<def title="suffix" id="@JvmBlocking-suffix">

`suffix` represents the suffix of the generated function.
By default, it is `Blocking`.

</def>
<def title="asProperty" id="@JvmBlocking-asProperty">

`asProperty` represents whether to generate a property instead of a function.
By default, it is `false`.

```Kotlin
suspend fun foo(): T = ...

// Generated
@Api4J
val fooBlocking: T 
    get() = runInBlocking { foo() }
```

> **Note**: If `asProperty` is `true`, the function cannot have parameters.

</def>
<def title="markName" id="@JvmBlocking-markName">

Refer to [](MarkName.md).

</def>
</deflist>


### JVM Async Transformer

The JVM Async transformer generates asynchronous variants using `CompletableFuture`.

#### Configuration {id=jvm-async-configuration}

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

#### Usage {id=jvm-async-usage}

<compare type="top-bottom" first-title="Source" second-title="Compiled">

```kotlin
class ApiService {
    @JvmAsync
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
}
```

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

</compare>

#### Key Features {id=jvm-async-key-features}

- **Default Generated Function Suffix**: `Async`
- **Return Type**: `CompletableFuture<out T>` where T is the original return type
- **Runtime Function**: `$runInAsync$`
- **Scope Handling**: Uses the current `CoroutineScope` if available, otherwise `GlobalScope`

#### Mark Annotation {id="jvm-async-mark-annotation"}

`@JvmAsync` provides some properties to change the default values and customize the generated function results.

<deflist>
<def title="baseName" id="@JvmAsync-baseName">

`baseName` represents the base name of the generated function.
By default, it is empty (`""`). If the value is empty, it means using the same value as the original function.

The final function name of the generated function is `baseName` + `suffix`.

</def>
<def title="suffix" id="@JvmAsync-suffix">

`suffix` represents the suffix of the generated function.
By default, it is `Async`.

</def>
<def title="asProperty" id="@JvmAsync-asProperty">

`asProperty` represents whether to generate a property instead of a function.
By default, it is `false`.

```Kotlin
suspend fun foo(): T = ...

// Generated
@Api4J
val fooAsync: CompletableFuture<out T> 
    get() = runInAsync { foo() }
```

> **Note**: If `asProperty` is `true`, the function cannot have parameters.

</def>
<def title="markName" id="@JvmAsync-markName">

Refer to [](MarkName.md).

</def>
</deflist>

## JavaScript Transformers

### JS Promise Transformer

The JS Promise transformer generates Promise-based variants for JavaScript interoperability.

#### Configuration {id=js-promise-configuration}

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

#### Usage {id=js-promise-usage}

<compare type="top-bottom" first-title="Source" second-title="Compiled">


```kotlin
class ApiService {
    @JsPromise
    suspend fun fetchData(): String {
        delay(1000)
        return "Data fetched"
    }
}
```

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

</compare>

#### Key Features {id=js-promise-key-features}

- **Default Generated Function Suffix**: `Async`
- **Return Type**: `Promise<T>` where T is the original return type
- **Runtime Function**: `$runInAsync$`

#### Mark Annotation {id="js-promise-mark-annotation"}

`@JsPromise` provides some properties to change the default values and customize the generated function results.

<deflist>
<def title="baseName" id="@JsPromise-baseName">

`baseName` represents the base name of the generated function.
By default, it is empty (`""`). If the value is empty, it means using the same value as the original function.

The final function name of the generated function is `baseName` + `suffix`.

</def>
<def title="suffix" id="@JsPromise-suffix">

`suffix` represents the suffix of the generated function.
By default, it is `Async`.

</def>
<def title="asProperty" id="@JsPromise-asProperty">

`asProperty` represents whether to generate a property instead of a function.
By default, it is `false`.

```Kotlin
suspend fun foo(): T = ...

// Generated
@Api4Js
val fooAsync: Promise<T> 
    get() = runInAsync { foo() }
```

> **Note**: If `asProperty` is `true`, the function cannot have parameters.

</def>
<def title="markName" id="@JsPromise-markName">

Refer to [](MarkName.md).

</def>
</deflist>

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

