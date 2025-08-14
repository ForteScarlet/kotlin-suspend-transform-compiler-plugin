---
id: mark-name
title: MarkName
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { ExperimentalBadge, VersionBadge } from '@site/src/components/Snippets';

<ExperimentalBadge></ExperimentalBadge>
<VersionBadge version="0.13.0"></VersionBadge>

You can use `markName` to add a name mark annotation (e.g. `@JvmName`, `@JsName`) to the generated synthetic function.

## Using in the standard runtime

### JVM Example

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class Foo {
    @JvmBlocking(markName = "namedWaitAndGet")
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
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }

    @Api4J // RequiresOptIn annotation, provide warnings to Kotlin
    @JvmName("namedWaitAndGet") // From the `markName`'s value
    fun waitAndGetBlocking(): String =
        runInBlocking { waitAndGet() } // 'runInBlocking' from the runtime provided by the plugin
}
```

  </TabItem>
</Tabs>

:::warning
`@JvmName` has limitations on non-final functions, and even the compiler may prevent compilation.
:::

### JavaScript Example

<Tabs>
  <TabItem value="source" label="Source">

```kotlin
class Foo {
    @JsPromise(markName = "namedWaitAndGet")
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
    @JsName("namedWaitAndGet") // From the `markName`'s value
    fun waitAndGetAsync(): Promise<String> =
        runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
}
```

  </TabItem>
</Tabs>


## Customize {#customize}

In custom annotations, you can configure `markName`.

:::info
For basic information about custom annotations (and transformers), 
refer to [Custom Transformers](../configuration/custom-transformers.md).
:::


:::warning
TODO
:::
