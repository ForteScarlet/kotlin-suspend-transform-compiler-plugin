---
id: default-transformers
title: 默认转换器
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

本指南介绍插件提供的默认转换器以及如何有效使用它们。

## 概述

转换器是定义挂起函数如何为不同平台转换的核心组件。
插件提供了几个内置转换器，涵盖了最常见的用例。

> **注意**：默认转换器依赖于插件提供的 `annotation` 和 `runtime` 依赖项。
> 在使用默认转换器之前，请确保在配置中包含它们。

## JVM 转换器

### JVM 阻塞转换器

JVM 阻塞转换器使用 `runBlocking` 生成挂起函数的阻塞变体。

#### 配置 {#jvm-blocking-configuration}

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

#### 用法 {#jvm-blocking-usage}

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

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

#### 主要特性 {#jvm-blocking-key-features}

- **默认生成函数后缀**：`Blocking`
- **返回类型**：与原函数相同
- **运行时函数**：`$runInBlocking$`（基于 `kotlinx.coroutines.runBlocking`）

#### 标记注解 {#jvm-blocking-mark-annotation}

`@JvmBlocking` 提供一些属性来更改默认值并自定义生成的函数结果。

##### baseName {#jvmblocking-basename}

`baseName` 表示生成函数的基本名称。
默认情况下，它是空的（`""`）。如果值为空，意味着使用与原函数相同的值。

生成函数的最终函数名是 `baseName` + `suffix`。

##### suffix {#jvmblocking-suffix}

`suffix` 表示生成函数的后缀。
默认情况下，它是 `Blocking`。

##### asProperty {#jvmblocking-asproperty}

`asProperty` 表示是否生成属性而不是函数。
默认情况下，它是 `false`。

```kotlin
suspend fun foo(): T = ...

// 生成的
@Api4J
val fooBlocking: T 
    get() = runInBlocking { foo() }
```

> **注意**：如果 `asProperty` 为 `true`，函数不能有参数。

##### markName {#jvmblocking-markname}

参考 [MarkName](../features/mark-name.md)。


### JVM 异步转换器

JVM 异步转换器使用 `CompletableFuture` 生成异步变体。

#### 配置 {#jvm-async-configuration}

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

#### 用法 {#jvm-async-usage}

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

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

#### 主要特性 {#jvm-async-key-features}

- **默认生成函数后缀**：`Async`
- **返回类型**：`CompletableFuture<out T>`，其中 T 是原返回类型
- **运行时函数**：`$runInAsync$`
- **作用域处理**：如果可用，使用当前 `CoroutineScope`，否则使用 `GlobalScope`

#### 标记注解 {#jvm-async-mark-annotation}

`@JvmAsync` 提供一些属性来更改默认值并自定义生成的函数结果。

##### baseName {#jvmasync-basename}

`baseName` 表示生成函数的基本名称。
默认情况下，它是空的（`""`）。如果值为空，意味着使用与原函数相同的值。

生成函数的最终函数名是 `baseName` + `suffix`。

##### suffix {#jvmasync-suffix}

`suffix` 表示生成函数的后缀。
默认情况下，它是 `Async`。

##### asProperty {#jvmasync-asproperty}

`asProperty` 表示是否生成属性而不是函数。
默认情况下，它是 `false`。

```kotlin
suspend fun foo(): T = ...

// 生成的
@Api4J
val fooAsync: CompletableFuture<out T> 
    get() = runInAsync { foo() }
```

> **注意**：如果 `asProperty` 为 `true`，函数不能有参数。

##### markName {#jvmasync-markname}

参考 [MarkName](../features/mark-name.md)。

## JavaScript 转换器

### JS Promise 转换器

JS Promise 转换器为 JavaScript 互操作性生成基于 Promise 的变体。

#### 配置 {#js-promise-configuration}

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

#### 用法 {#js-promise-usage}

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

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

#### 主要特性 {#js-promise-key-features}

- **默认生成函数后缀**：`Async`
- **返回类型**：`Promise<T>`，其中 T 是原返回类型
- **运行时函数**：`$runInAsync$`

#### 标记注解 {#js-promise-mark-annotation}

`@JsPromise` 提供一些属性来更改默认值并自定义生成的函数结果。

##### baseName {#jspromise-basename}

`baseName` 表示生成函数的基本名称。
默认情况下，它是空的（`""`）。如果值为空，意味着使用与原函数相同的值。

生成函数的最终函数名是 `baseName` + `suffix`。

##### suffix {#jspromise-suffix}

`suffix` 表示生成函数的后缀。
默认情况下，它是 `Async`。

##### asProperty {#jspromise-asproperty}

`asProperty` 表示是否生成属性而不是函数。
默认情况下，它是 `false`。

```kotlin
suspend fun foo(): T = ...

// 生成的
@Api4Js
val fooAsync: Promise<T> 
    get() = runInAsync { foo() }
```

> **注意**：如果 `asProperty` 为 `true`，函数不能有参数。

##### markName {#jspromise-markname}

参考 [MarkName](../features/mark-name.md)。

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
