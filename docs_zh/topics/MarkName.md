# MarkName

<primary-label ref="experimental" xmlns=""/>
<secondary-label ref="version-0.13.0" />

您可以使用 `markName` 向生成的合成函数添加名称标记注解（例如 `@JvmName`、`@JsName`）。

## 在标准运行时中使用

### JVM 示例

<compare type="top-bottom" first-title="源代码" second-title="编译后">

```kotlin
class Foo {
    @JvmBlocking(markName = "namedWaitAndGet")
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
}
```

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

</compare>

<warning>

`@JvmName` 对非 final 函数有限制，甚至编译器可能会阻止编译。

</warning>

### JavaScript 示例

<compare type="top-bottom" first-title="源代码" second-title="编译后">

```kotlin
class Foo {
    @JsPromise(markName = "namedWaitAndGet")
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
}
```

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

</compare>


## 自定义

在自定义注解中，您可以配置 `markName`。

> 有关自定义注解（和转换器）的基本信息，
> 请参考 [](Custom-Transformers.md)。

<warning>
TODO
</warning>