# 默认转换器

本指南介绍插件提供的默认转换器以及如何有效使用它们。

## 概述

转换器是定义挂起函数如何为不同平台转换的核心组件。
插件提供了几个内置转换器，涵盖了最常见的用例。

> **注意**：默认转换器依赖于插件提供的 `annotation` 和 `runtime` 依赖项。
> 在使用默认转换器之前，请确保在配置中包含它们。

## JVM 转换器

### JVM 阻塞转换器

JVM 阻塞转换器使用 `runBlocking` 生成挂起函数的阻塞变体。

#### 配置 {id=jvm-阻塞-配置}

```kotlin
suspendTransformPlugin {
    transformers {
        // 方式 1：简单添加
        addJvmBlocking()

        // 方式 2：使用配置对象
        addJvm(SuspendTransformConfigurations.jvmBlockingTransformer)
        
        // 方式 3：使用平台特定添加
        add(
            TargetPlatform.JVM, 
            SuspendTransformConfigurations.jvmBlockingTransformer
        )
    }
}
```

#### 用法 {id=jvm-阻塞-用法}

<compare type="top-bottom" first-title="源代码" second-title="编译后" xmlns="">

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

#### 主要特性 {id=jvm-阻塞-主要特性}

- **默认生成函数后缀**：`Blocking`
- **返回类型**：与原函数相同
- **运行时函数**：`$runInBlocking$`（基于 `kotlinx.coroutines.runBlocking`）

#### 标记注解 {id="jvm-阻塞-标记注解"}

`@JvmBlocking` 提供一些属性来更改默认值并自定义生成的函数结果。

<deflist>
<def title="baseName" id="@JvmBlocking-baseName">

`baseName` 表示生成函数的基本名称。
默认情况下，它是空的（`""`）。如果值为空，意味着使用与原函数相同的值。

生成函数的最终函数名是 `baseName` + `suffix`。

</def>
<def title="suffix" id="@JvmBlocking-suffix">

`suffix` 表示生成函数的后缀。
默认情况下，它是 `Blocking`。

</def>
<def title="asProperty" id="@JvmBlocking-asProperty">

`asProperty` 表示是否生成属性而不是函数。
默认情况下，它是 `false`。

```Kotlin
suspend fun foo(): T = ...

// 生成的
@Api4J
val fooBlocking: T 
    get() = runInBlocking { foo() }
```

> **注意**：如果 `asProperty` 为 `true`，函数不能有参数。

</def>
<def title="markName" id="@JvmBlocking-markName">

参考 [](MarkName.md)。

</def>
</deflist>


### JVM 异步转换器

JVM 异步转换器使用 `CompletableFuture` 生成异步变体。

#### 配置 {id=jvm-异步-配置}

```kotlin
suspendTransformPlugin {
    transformers {
        // 方式 1：简单添加
        addJvmAsync()

        // 方式 2：使用配置对象
        addJvm(SuspendTransformConfigurations.jvmAsyncTransformer)
        
        // 方式 3：使用平台特定添加
        add(
            TargetPlatform.JVM, 
            SuspendTransformConfigurations.jvmAsyncTransformer
        )
    }
}
```

#### 用法 {id=jvm-异步-用法}

<compare type="top-bottom" first-title="源代码" second-title="编译后">

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

#### 主要特性 {id=jvm-异步-主要特性}

- **默认生成函数后缀**：`Async`
- **返回类型**：`CompletableFuture<out T>`，其中 T 是原返回类型
- **运行时函数**：`$runInAsync$`
- **作用域处理**：如果可用，使用当前 `CoroutineScope`，否则使用 `GlobalScope`

#### 标记注解 {id="jvm-异步-标记注解"}

`@JvmAsync` 提供一些属性来更改默认值并自定义生成的函数结果。

<deflist>
<def title="baseName" id="@JvmAsync-baseName">

`baseName` 表示生成函数的基本名称。
默认情况下，它是空的（`""`）。如果值为空，意味着使用与原函数相同的值。

生成函数的最终函数名是 `baseName` + `suffix`。

</def>
<def title="suffix" id="@JvmAsync-suffix">

`suffix` 表示生成函数的后缀。
默认情况下，它是 `Async`。

</def>
<def title="asProperty" id="@JvmAsync-asProperty">

`asProperty` 表示是否生成属性而不是函数。
默认情况下，它是 `false`。

```Kotlin
suspend fun foo(): T = ...

// 生成的
@Api4J
val fooAsync: CompletableFuture<out T> 
    get() = runInAsync { foo() }
```

> **注意**：如果 `asProperty` 为 `true`，函数不能有参数。

</def>
<def title="markName" id="@JvmAsync-markName">

参考 [](MarkName.md)。

</def>
</deflist>

## JavaScript 转换器

### JS Promise 转换器

JS Promise 转换器为 JavaScript 互操作性生成基于 Promise 的变体。

#### 配置 {id=js-promise-配置}

```kotlin
suspendTransformPlugin {
    transformers {
        // 方式 1：简单添加
        addJsPromise()

        // 方式 2：使用配置对象
        addJs(SuspendTransformConfigurations.jsPromiseTransformer)
        
        // 方式 3：使用平台特定添加
        add(
            TargetPlatform.JS, 
            SuspendTransformConfigurations.jsPromiseTransformer
        )
    }
}
```

#### 用法 {id=js-promise-用法}

<compare type="top-bottom" first-title="源代码" second-title="编译后">


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

#### 主要特性 {id=js-promise-主要特性}

- **默认生成函数后缀**：`Async`
- **返回类型**：`Promise<T>`，其中 T 是原返回类型
- **运行时函数**：`$runInAsync$`

#### 标记注解 {id="js-promise-标记注解"}

`@JsPromise` 提供一些属性来更改默认值并自定义生成的函数结果。

<deflist>
<def title="baseName" id="@JsPromise-baseName">

`baseName` 表示生成函数的基本名称。
默认情况下，它是空的（`""`）。如果值为空，意味着使用与原函数相同的值。

生成函数的最终函数名是 `baseName` + `suffix`。

</def>
<def title="suffix" id="@JsPromise-suffix">

`suffix` 表示生成函数的后缀。
默认情况下，它是 `Async`。

</def>
<def title="asProperty" id="@JsPromise-asProperty">

`asProperty` 表示是否生成属性而不是函数。
默认情况下，它是 `false`。

```Kotlin
suspend fun foo(): T = ...

// 生成的
@Api4Js
val fooAsync: Promise<T> 
    get() = runInAsync { foo() }
```

> **注意**：如果 `asProperty` 为 `true`，函数不能有参数。

</def>
<def title="markName" id="@JsPromise-markName">

参考 [](MarkName.md)。

</def>
</deflist>

## 便利函数

### 组合 JVM 转换器

```kotlin
suspendTransformPlugin {
    transformers {
        // 包括 addJvmBlocking() 和 addJvmAsync()
        useJvmDefault()
    }
}
```

这等同于：

```kotlin
suspendTransformPlugin {
    transformers {
        addJvmBlocking()
        addJvmAsync()
    }
}
```

### 组合 JS 转换器

```kotlin
suspendTransformPlugin {
    transformers {
        // 包括 addJsPromise()
        useJsDefault()
    }
}
```

### 所有默认转换器

```kotlin
suspendTransformPlugin {
    transformers {
        // 包括所有平台的所有默认转换器
        useDefault()
    }
}
```

这等同于：

```kotlin
suspendTransformPlugin {
    transformers {
        useJvmDefault()  // JVM 阻塞 + JVM 异步
        useJsDefault()   // JS Promise
    }
}
```

## 组合多个注解

您可以在同一个函数上使用多个转换器注解：

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

这将生成：
- `fetchDataBlocking(): String` (JVM)
- `fetchDataAsync(): CompletableFuture<out String>` (JVM)
- `fetchDataAsync(): Promise<String>` (JS)