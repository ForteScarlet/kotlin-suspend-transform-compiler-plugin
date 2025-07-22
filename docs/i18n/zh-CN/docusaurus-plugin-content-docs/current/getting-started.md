---
id: getting-started
title: 快速开始
sidebar_position: 3
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { ExperimentalBadge, VersionBadge } from '@site/src/components/Snippets';

本指南将引导您了解 Kotlin 挂起函数转换编译器插件在不同平台上的基本用法。

## 配置

首先，通过配置Gradle插件来启用默认的配置。

::::note

有关更多配置的信息，可前往参考 [配置](./configuration/configuration.md)。

::::

**build.gradle.kts**

```kotlin
suspendTransformPlugin {
    // `enable = true` 是默认的
    transformers {
        // 使用全部的默认转化器
        useDefault()

        // 或精确的选择要启用的转化器:
        // addJvmBlocking()
        // addJvmAsync()
        // addJsPromise()
    }
}
```

## JVM 平台

JVM 平台支持挂起函数的阻塞和异步转换。

### 基本用法 {#jvm-basic-usage}

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

```kotlin
class Foo {
    // 对 Java 隐藏
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    
    @Api4J // RequiresOptIn 注解，为 Kotlin 提供警告
    fun waitAndGetBlocking(): String = runInBlocking { waitAndGet() } // 'runInBlocking' 来自插件提供的运行时
    
    @Api4J // RequiresOptIn 注解，为 Kotlin 提供警告
    fun waitAndGetAsync(): CompletableFuture<out String> = runInAsync { waitAndGet() } // 'runInAsync' 来自插件提供的运行时
}
```

  </TabItem>
</Tabs>

## JavaScript 平台

JavaScript 平台支持基于 Promise 的转换。

### 基本用法 {#js-basic-usage}

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

```kotlin
class Foo {
    
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    
    @Api4Js // RequiresOptIn 注解，为 Kotlin 提供警告
    fun waitAndGetAsync(): Promise<String> = runInAsync { waitAndGet() } // 'runInAsync' 来自插件提供的运行时
}
```

  </TabItem>
</Tabs>

:::note
JS 平台支持在版本 0.6.0 中添加！查看开发过程请参考 [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)，最终实现请参考 [#39](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/39)！
:::


## WasmJS 平台

<ExperimentalBadge />
<VersionBadge version="0.6.0" />

::::warning
下面 WasmJS 示例中提到的转换器函数
是假设由您定义的自定义函数。

它们不包含在 `runtime` 中。

由于在 WasmJS 中使用各种类型有很多限制...
所以我还不确定如何完美地处理它们。
在那之前，您可以自定义函数和类型来控制编译器插件的行为。
就像您可以自定义其他平台一样。
::::

```kotlin
// 一些由您定义的自定义转换器函数...
fun <T> runInAsync(block: suspend () -> T): AsyncResult<T> = AsyncResult(block)

class AsyncResult<T>(val block: suspend () -> T) {
    @OptIn(DelicateCoroutinesApi::class)
    fun toPromise(): Promise<JsAny?> {
        return GlobalScope.promise { block() }
    }
}
```

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

```kotlin
class Foo {
    
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    
    @Api4Js // RequiresOptIn 注解，为 Kotlin 提供警告
    fun waitAndGetAsync(): AsyncResult<String> = runInAsync { waitAndGet() } // 'runInAsync' 来自插件提供的运行时
}
```

  </TabItem>
</Tabs>
