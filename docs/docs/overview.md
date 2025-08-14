---
id: overview
title: Overview
slug: /
sidebar_position: 1
---

import { CoverImage } from '@site/src/components/Snippets';

[![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

<CoverImage />

## Summary

Kotlin compiler plugin for generating platform-compatible functions for suspend functions.

## Quick Example

### JVM Platform

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

The plugin automatically generates:

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

### JavaScript Platform

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
}
```

The plugin automatically generates:

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

## Platform Support Status

| Platform              | Since Version |
|-----------------------|---------------|
| JVM                   | 0.1.0         |
| JavaScript            | 0.6.0         |
| WasmJS (Experimental) | 0.6.0         |

:::note
JS platform support was added in version 0.6.0. See the implementation details
at [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)
and [PR #39](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/39).
:::

