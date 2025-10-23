# Kotlin suspend transform compiler plugin
[![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/) 
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

<img src=".project/cover.png" alt="cover">

[GitHub](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin) | [Gitee](https://gitee.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin)

**English** | [简体中文](README_CN.md)

## What's this

This is a Kotlin compiler plugin for generating platform-compatible functions for suspend functions.

### JVM

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

compiled 👇

```kotlin
class Foo {
    // Hide from Java
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    @Api4J // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetBlocking(): String = runInBlocking { waitAndGet() } // 'runInBlocking' from the runtime provided by the plugin

    @Api4J // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetAsync(): CompletableFuture<out String> = runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
}
```

### JS

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}
```

compiled 👇

```kotlin
class Foo {
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    @Api4Js // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetAsync(): Promise<String> = runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
}
```

> ~~JS platform target not supported yet. see: [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)~~
>
> JS has been supported since 0.6.0! See the process at [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993), and the final winning shot at [#39](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/39)!

### WasmJS

> [!warning]
> Since `v0.6.0`, In experiments, immature and unstable

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}

// Some functions or types customised by **you**...
// They are not included in the runtime. 
// Since there are a lot of restrictions on the use of various types in WasmJS...
// so I'm not sure how to handle them perfectly yet.
// Until then, you can customise functions and types to control the behaviour of the compiler plugin yourself.
// just like you can customise other platforms.

fun <T> runInAsync(block: suspend () -> T): AsyncResult<T> = AsyncResult(block)

class AsyncResult<T>(val block: suspend () -> T) {
    @OptIn(DelicateCoroutinesApi::class)
    fun toPromise(): Promise<JsAny?> {
        return GlobalScope.promise { block() }
    }
}
```

compiled 👇

```kotlin
class Foo {
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    @Api4Js // RequiresOptIn annotation, provide warnings to Kotlin
    fun waitAndGetAsync(): AsyncResult<String> = runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
    // AsyncResult is a custom type by **you**
}
```

### MarkName

> since v0.13.0, [#96](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/96)

You can use `markName` to add a name mark annotation (e.g. `@JvmName`, `@JsName`) to the generated synthetic function.

For example the JVM:

```kotlin
class Foo {
    @JvmBlocking(markName = "namedWaitAndGet")
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}
```

compiled 👇

```kotlin
class Foo {
    // Hide from Java
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    @Api4J // RequiresOptIn annotation, provide warnings to Kotlin
    @JvmName("namedWaitAndGet") // From the `markName`'s value
    fun waitAndGetBlocking(): String = runInBlocking { waitAndGet() } // 'runInBlocking' from the runtime provided by the plugin
}
```

Note: `@JvmName` has limitations on non-final functions, and even the compiler may prevent compilation.

For example the JS:

```kotlin
class Foo {
    @JsPromise(markName = "namedWaitAndGet")
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}
```

compiled 👇

```kotlin
class Foo {
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
  
    @Api4Js // RequiresOptIn annotation, provide warnings to Kotlin
    @JsName("namedWaitAndGet") // From the `markName`'s value
    fun waitAndGetAsync(): Promise<String> = runInAsync { waitAndGet() } // 'runInAsync' from the runtime provided by the plugin
}
```

## Documentation

This is the [documentation](https://kstcp.forte.love/).

> [!note]
> If you notice any issues or omissions in the documentation,
> feel free to [provide feedback](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues) anytime!

## Use Cases

- [Simple Robot Frameworks](https://github.com/simple-robot/simpler-robot) (Fully customized)


## License

see [LICENSE](LICENSE) .

```text
Copyright (c) 2022 ForteScarlet

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
