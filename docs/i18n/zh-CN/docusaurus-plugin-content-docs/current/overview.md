---
id: overview
title: Kotlin 挂起函数转换编译器插件
slug: /
---

import { CoverImage } from '@site/src/components/Snippets';

[![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

<CoverImage />

## 概述

用于为挂起函数生成平台兼容函数的 Kotlin 编译器插件。

## 快速示例

### JVM 平台

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

插件会自动生成：

```kotlin
class Foo {
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }

    @Api4J
    fun waitAndGetBlocking(): String = runInBlocking { waitAndGet() }

    @Api4J
    fun waitAndGetAsync(): CompletableFuture<out String> = runInAsync { waitAndGet() }
}
```

### JavaScript 平台

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
}
```

插件会自动生成：

```kotlin
class Foo {
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }

    @Api4Js
    fun waitAndGetAsync(): Promise<String> = runInAsync { waitAndGet() }
}
```

## 平台支持状态

| 平台                  | 起始版本      |
|-----------------------|---------------|
| JVM                   | 0.1.0         |
| JavaScript            | 0.6.0         |
| WasmJS (实验性)       | 0.6.0         |

> **注意**：JS 平台支持在版本 0.6.0 中添加。查看实现详情请参考
> [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)
> 和 [PR #39](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/39)。
