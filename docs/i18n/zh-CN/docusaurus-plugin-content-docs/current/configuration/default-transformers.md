---
id: default-transformers
title: 默认转换器
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { VersionBadge } from '@site/src/components/Snippets';

本指南介绍插件提供的默认转换器以及如何有效使用它们。

## 概述

转换器是定义挂起函数如何为不同平台转换的核心组件。
插件提供了几个内置转换器，涵盖了最常见的用例。

:::note
默认转换器依赖于插件提供的 `annotation` 和 `runtime` 依赖项。
在使用默认转换器之前，请确保在配置中包含它们。
:::


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

#### 生命周期与调度 {#jvm-blocking-lifecycle}

生成的 blocking bridge 只面向 Java 风格的阻塞互操作。它会阻塞调用线程，直到
挂起函数完成。线程中断会取消 bridge，并以 `InterruptedException` 报告。

默认 runtime 使用 `runBlocking(Dispatchers.IO)`。这会在调度后让 suspend 主体
离开调用线程执行，是面对可能阻塞的工作时较保守的默认值；但调用线程仍然会被
阻塞。不要在 coroutine、UI/event-loop 线程或其他线程受限的执行路径中调用生成的
blocking bridge。

如果需要特定 dispatcher、事务上下文、MDC 或线程亲和性，请使用自定义
transformer/runtime。

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

:::note
如果 `asProperty` 为 `true`，函数不能有参数。
:::


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

#### 生命周期与取消 {#jvm-async-lifecycle}

如果 receiver 是 `CoroutineScope`，生成的 bridge 会在该 scope 中启动 coroutine。
否则，默认 runtime 使用 `GlobalScope`。

取消返回的 `CompletableFuture` 会取消 coroutine。丢弃一个长期运行的 future 但不
取消它，并不会停止底层工作；Java 调用方应保存并在不再需要时取消返回的 future。

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

:::note
如果 `asProperty` 为 `true`，函数不能有参数。
:::


##### markName {#jvmasync-markname}

参考 [MarkName](../features/mark-name.md)。

### JVM Reactive 转换器

<VersionBadge version="0.14.0" />

JVM Reactive 转换器会生成 Reactive Streams `Publisher` 变体。

#### 配置 {#jvm-reactive-configuration}

```kotlin
suspendTransformPlugin {
    transformers {
        // 方式 1：简单添加
        addJvmReactive()

        // 方式 2：使用配置对象
        addJvm(SuspendTransformConfigurations.jvmReactiveTransformer)
    }
}
```

#### 用法 {#jvm-reactive-usage}

<Tabs>
  <TabItem value="source" label="源代码">

```kotlin
@OptIn(ExperimentalJvmApi::class)
class ApiService {
    @JvmReactive
    suspend fun fetchData(): String? = null
}
```

  </TabItem>
  <TabItem value="compiled" label="编译后">

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

#### 主要特性 {#jvm-reactive-key-features}

- **默认生成函数后缀**：`Reactive`
- **返回类型**：`Publisher<T & Any>`，其中 T 是原返回类型
- **运行时函数**：`$runInReactive$`
- **null 处理**：`null` 结果会空完成
- **作用域处理**：生成调用可能传入 `CoroutineScope`，但默认 runtime 仅为生成
  bridge 兼容性和未来运行时策略保留该参数，不会把它作为 coroutine parent 或
  dispatcher 来源。

#### 生命周期与取消 {#jvm-reactive-lifecycle}

返回的 `Publisher` 是 cold publisher：subscriber 订阅时才会启动 suspend block。
取消 Reactive Streams `Subscription` 会取消 publisher coroutine。

默认 bridge 不配置 reactive 调度。需要 dispatcher 或生命周期约束时，应使用调用方
reactive 链、在 suspend 函数内部使用 `withContext`，或使用自定义
transformer/runtime。

:::note
这个转换器需要 JVM classpath 中包含
[`org.reactivestreams.Publisher`](https://www.reactive-streams.org/) 和
[`org.jetbrains.kotlinx:kotlinx-coroutines-reactive`](https://github.com/Kotlin/kotlinx.coroutines/blob/master/reactive/README.md)。
这些依赖不会由 `addJvmReactive()` 自动添加；需要由用户 JVM 项目或 source set 自行添加。
:::

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

:::note
如果 `asProperty` 为 `true`，函数不能有参数。
:::


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

`useJvmDefault()` 不包含 `addJvmReactive()`，如需 JVM Reactive 需要显式启用。

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
    @JvmReactive
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
- `fetchDataReactive(): Publisher<String>` (JVM)
- `fetchDataAsync(): Promise<String>` (JS)
