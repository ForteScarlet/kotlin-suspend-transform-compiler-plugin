---
id: mark-name
title: MarkName
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { Badge } from '@site/src/components/Snippets';

<Badge type="primary">实验性</Badge>
<Badge type="secondary">版本 0.13.0</Badge>

您可以使用 `markName` 向生成的合成函数添加名称标记注解（例如 `@JvmName`、`@JsName`）。

## 在标准运行时中使用

### JVM 示例

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

```kotlin
class Foo {
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }

    @Api4J // RequiresOptIn 注解，为 Kotlin 提供警告
    @JvmName("namedWaitAndGet") // 来自 `markName` 的值
    fun waitAndGetBlocking(): String =
        runInBlocking { waitAndGet() } // 'runInBlocking' 来自插件提供的运行时
}
```

  </TabItem>
</Tabs>

:::warning
`@JvmName` 对非 final 函数有限制，甚至编译器可能会阻止编译。
:::

### JavaScript 示例

<Tabs>
  <TabItem value="source" label="源代码">

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
  <TabItem value="compiled" label="编译后">

```kotlin
class Foo {
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }

    @Api4Js // RequiresOptIn 注解，为 Kotlin 提供警告
    @JsName("namedWaitAndGet") // 来自 `markName` 的值
    fun waitAndGetAsync(): Promise<String> =
        runInAsync { waitAndGet() } // 'runInAsync' 来自插件提供的运行时
}
```

  </TabItem>
</Tabs>


## 自定义 {#customize}

在自定义注解中，您可以配置 `markName`。

:::info
有关自定义注解（和转换器）的基本信息，
请参考 [自定义转换器](../configuration/custom-transformers.md)。
:::


:::warning
TODO
:::
