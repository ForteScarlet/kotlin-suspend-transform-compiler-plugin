# Kotlin suspend transform compiler plugin
[![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/) 
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

<img src=".project/cover.png" alt="封面">

[GitHub](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin) | [Gitee](https://gitee.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin)

[English](README.md) | **简体中文**

## 简介

用于为Kotlin挂起函数自动生成平台兼容函数的Kotlin编译器插件。

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

编译后 👇

```kotlin
class Foo {
    // 对Java隐藏
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    @Api4J // RequiresOptIn 注解, 向Kotlin开发者提供警告
    fun waitAndGetBlocking(): String = runInBlocking { waitAndGet() } // 'runInBlocking' 来自于插件提供的运行时依赖

    @Api4J // RequiresOptIn 注解, 向Kotlin开发者提供警告
    fun waitAndGetAsync(): CompletableFuture<out String> = runInAsync { waitAndGet() } // 'runInAsync' 来自于插件提供的运行时依赖
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

编译后 👇

```kotlin
class Foo {
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    @Api4Js // RequiresOptIn 注解, 向Kotlin开发者提供警告
    fun waitAndGetAsync(): Promise<String> = runInAsync { waitAndGet() } // 'runInAsync' 来自于插件提供的运行时依赖
}
```

> ~~JS 目标平台暂不支持。原因参考: [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)~~
> 
> JS 平台从 `v0.6.0` 版本开始得到支持。 参考 [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993) 了解过程, 以及从 [#39](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/39) 查阅制胜一击!

### WasmJS

> [!warning]
> 从 `v0.6.0` 开始支持，实验中，不成熟、不稳定。

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}

// 一些由**你**自定义的函数或类型...
// 它们不包含在 runtime 中。由于在 WasmJS 中，对于各种类型的使用会有很多限制，
// 因此我还不清楚如何完美地处理它们。
// 在那之前，你可以自定义函数和类型来自行控制编译器插件的行为，
// 就像自定义其他平台那样。

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
    // AsyncResult is a custom type
}
```


## 使用

### 版本说明

在 `0.9.0` (包括) 以前，版本的命名方式是 `x.y.z` 的形式。
但是Kotlin编译器的内容集合每个Kotlin版本都有可能发生改变，
而这似乎无法体现出其构建于的Kotlin版本信息，进而导致产生一些混乱。

因此，从 `0.9.0` 之后的版本开始，版本的命名方式会改为 `$Kotlin-$plugin` 的形式，
例如 `2.0.20-0.9.1`。
前半部分代表用于构建的Kotlin版本，而后半部分则为插件的版本。

如果版本小于等于 `0.9.0`，你可以参考下面这个对照表：

| Kotlin版本 | 插件版本                    |
|----------|-------------------------|
| `2.0.0`  | `0.8.0-beta1` ~ `0.9.0` |
| `1.9.22` | `0.7.0-beta1`           |
| `1.9.21` | `0.6.0`                 |
| `1.9.10` | `0.5.1`                 |
| `1.9.0`  | `0.5.0`                 |
| `1.8.21` | `0.3.1` ~ `0.4.0`       |

> [!note]
> 我没有详细记录每一个Kotlin版本之间的编译器的兼容性。
> 根据我的记忆和猜测，每当 minor 版本号增加时 (例如 `1.8.0` -> `1.9.0`)
> 则不兼容的概率较大，而当 patch 增加时 (例如 `1.9.0` -> `1.9.10`) 不兼容的概率较小。

### Gradle

**通过 [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block) 使用:**

_build.gradle.kts_

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "$KOTLIN_VERSION" // or js? or multiplatform?
    id("love.forte.plugin.suspend-transform") version "$PLUGIN_VERSION"
    // other...
}

// other...

// config it.
suspendTransform {
    enabled = true // default: true
    includeRuntime = true // default: true
    includeAnnotation = true // default: true
    // 注意：如果禁用 includeAnnotation, 你需要自定义 targetMarker 或将其设置为 `null`
    //  更多参考: https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/73
    // targetMarker = null // 或自定义
    
    /*
     * 相当于同时使用 `useJvmDefault` 和 `useJsDefault`.
     * 需要包含 runtime 和 annotation
     */
    // useDefault()

    /*
     * 使用JVM平台的默认配置
     * 相当于:
     * addJvmTransformers(
     *     SuspendTransformConfiguration.jvmBlockingTransformer,
     *     SuspendTransformConfiguration.jvmAsyncTransformer,
     * )
     *
     * 需要包含 runtime 和 annotation
     */
    useJvmDefault()

    // 或者由你自定义
    jvm {
        // ...
    }
    // 或者由你自定义
    addJvmTransformers(...)

    /*
     * 使用JS平台的默认配置
     * 相当于:
     * addJvmTransformers(
     *     SuspendTransformConfiguration.jsPromiseTransformer,
     * )
     *
     * 需要包含 runtime 和 annotation
     */
    useJsDefault()

    // 或者由你自定义
    js {
        // ...
    }
    // 或者由你自定义
    addJsTransformers(...)
}
```



**通过 [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application) 使用:**

_build.gradle.kts_

```kotlin
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:$GRADLE_PLUGIN_VERSION")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") // 或 js? 或 multiplatform?
    id("love.forte.plugin.suspend-transform") 
    // 其他...
}

// 其他...

// 配置
suspendTransform {
    enabled = true // default: true
    includeRuntime = true // default: true
    includeAnnotation = true // default: true
    // 注意：如果禁用 includeAnnotation, 你需要自定义 targetMarker 或将其设置为 `null`
    //  更多参考: https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/73
    // targetMarker = null // 或自定义

    /*
     * 相当于同时使用 `useJvmDefault` 和 `useJsDefault`.
     * 需要包含 runtime 和 annotation
     */
    // useDefault()

    /*
     * 使用JVM平台的默认配置
     * 相当于:
     * addJvmTransformers(
     *     SuspendTransformConfiguration.jvmBlockingTransformer,
     *     SuspendTransformConfiguration.jvmAsyncTransformer,
     * )
     *
     * 需要包含 runtime 和 annotation
     */
    useJvmDefault()

    // 或者由你自定义
    jvm {
        // ...
    }
    // 或者由你自定义
    addJvmTransformers(...)

    /*
     * 使用JS平台的默认配置
     * 相当于:
     * addJvmTransformers(
     *     SuspendTransformConfiguration.jsPromiseTransformer,
     * )
     *
     * 需要包含 runtime 和 annotation
     */
    useJsDefault()

    // 或者由你自定义
    js {
        // ...
    }
    // 或者由你自定义
    addJsTransformers(...)
}
```

## 注意事项

### Gradle JVM

Gradle JVM 必须满足 JDK11+

### K2

K2 编译器从 `v0.7.0` 开始支持。

> [!warning]
> 实验中。

### JsExport

如果你打算在默认配置的情况下使用 `@JsExport`, 可以尝试以下代码：

_build.gradle.kts_

```kotlin
plugins {
    ...
}

suspendTransform {
    addJsTransformers(
        SuspendTransformConfiguration.jsPromiseTransformer.copy(
            copyAnnotationExcludes = listOf(
                // 生成的函数将不会包含 `@JsExport.Ignore`
                ClassInfo("kotlin.js", "JsExport.Ignore")
            )
        )
    )
}
```

```Kotlin
@file:OptIn(ExperimentalJsExport::class)

@JsExport
class Foo {
    @JsPromise
    @JsExport.Ignore
    suspend fun run(): Int = ...
}
```

## 效果

**源代码:**

```kotlin
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

@JvmBlocking
@JvmAsync
interface Foo {

    suspend fun name(): String

    suspend fun age(def: Int = 5): Int

    @JvmBlocking(asProperty = true)
    suspend fun self(): Foo
}

@JvmBlocking
@JvmAsync
class FooImpl : Foo {
    suspend fun size(): Long = 666
    override suspend fun name(): String = "forte"
    override suspend fun age(def: Int): Int = def
    @JvmBlocking(asProperty = true) // asProperty 必须为 true
    override suspend fun self(): FooImpl = this
}

class Bar {
    @JvmBlocking
    @JvmAsync
    suspend fun bar(): String = ""

    suspend fun noTrans(): Int = 1
}
```

**编译结果:**

> _简化自反编译结果_

```kotlin
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.plugin.suspendtrans.annotation.Generated
import love.forte.plugin.suspendtrans.annotation.Api4J
import kotlin.jvm.JvmSynthetic

@JvmBlocking 
@JvmAsync
interface Foo {
    @Generated 
    @Api4J 
    val selfBlocking: Foo /* compiled code */

    suspend fun age(def: Int /* = compiled code */): Int

    @Generated 
    @Api4J 
    fun ageAsync(def: Int /* = compiled code */): java.util.concurrent.CompletableFuture<Int> { /* compiled code */ }

    @Generated 
    @Api4J 
    fun ageBlocking(def: Int /* = compiled code */): Int { /* compiled code */ }

    suspend fun name(): String

    @Generated 
    @Api4J 
    fun nameAsync(): java.util.concurrent.CompletableFuture<out String> { /* compiled code */ }

    @Generated 
    @Api4J 
    fun nameBlocking(): String { /* compiled code */ }

    @JvmBlocking 
    suspend fun self(): Foo

    @Generated 
    @Api4J 
    fun selfAsync(): java.util.concurrent.CompletableFuture<out Foo> { /* compiled code */ }
}

@JvmBlocking 
@JvmAsync 
class FooImpl : Foo {
    @Generated 
    @Api4J 
    open val selfBlocking: FooImpl /* compiled code */

    @JvmSynthetic
    open suspend fun age(def: Int): Int { /* compiled code */ }

    @Generated 
    @Api4J 
    open fun ageAsync(def: Int): java.util.concurrent.CompletableFuture<Int> { /* compiled code */ }

    @Generated 
    @Api4J 
    open fun ageBlocking(def: Int): Int { /* compiled code */ }

    @JvmSynthetic
    open suspend fun name(): String { /* compiled code */ }

    @Generated 
    @Api4J 
    open fun nameAsync(): java.util.concurrent.CompletableFuture<out String> { /* compiled code */ }

    @Generated 
    @Api4J 
    open fun nameBlocking(): String { /* compiled code */ }

    @JvmSynthetic
    @JvmBlocking 
    suspend fun self(): FooImpl { /* compiled code */ }

    @Generated 
    @Api4J
    fun selfAsync(): java.util.concurrent.CompletableFuture<out FooImpl> { /* compiled code */ }

    @JvmSynthetic
    suspend fun size(): Long { /* compiled code */ }

    @Generated 
    @Api4J
    fun sizeAsync(): java.util.concurrent.CompletableFuture<Long> { /* compiled code */ }

    @Generated 
    @Api4J
    fun sizeBlocking(): Long { /* compiled code */ }
}


class Bar {
    @JvmSynthetic
    @JvmBlocking 
    @JvmAsync
    suspend fun bar(): String { /* compiled code */ }

    @Generated 
    @Api4J 
    fun barAsync(): java.util.concurrent.CompletableFuture<out String> { /* compiled code */ }

    @Generated 
    @Api4J 
    fun barBlocking(): String { /* compiled code */ }

    fun noTrans(): Int { /* compiled code */ }
}
```

## 自定义配置


```kotlin
plugin {
    id("love.forte.plugin.suspend-transform") version "$VERSION"
}


suspendTransform {
    // enabled suspend transform plugin
    enabled = true
    // include 'love.forte.plugin.suspend-transform:suspend-transform-runtime' to the runtime environment
    includeRuntime = true
    // the configuration name for including 'love.forte.plugin.suspend-transform:suspend-transform-runtime'
    runtimeConfigurationName = "implementation"

    val customJvmTransformer = Transformer(
        // mark annotation info, e.g. `@JvmBlocking`
        markAnnotation = MarkAnnotation(
            classInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "JvmBlocking"), // class info for this annotation
            baseNameProperty = "baseName",      // The property used to represent the 'base name' in the annotation, e.g. `@JvmBlocking(baseName = ...)`
            suffixProperty = "suffix",          // The property used to represent the 'suffix' in the annotation, e.g. `@JvmBlocking(suffix = ...)`
            asPropertyProperty = "asProperty",  // The property used to represent the 'asProperty' in the annotation, e.g. `@JvmBlocking(asProperty = true|false)`
            defaultSuffix = "Blocking",         // Default value used when property 'suffix' (the value of suffixProperty) does not exist (when not specified by the user) (the compiler plugin cannot detect property defaults directly, so the default value must be specified from here)
            // e.g. @JvmBlocking(suffix = "Abc"), the suffix is 'Abc', but `@JvmBlocking()`, the suffix is null in compiler plugin, so use the default suffix value.
            defaultAsProperty = false,          // Default value used when property 'suffix' (the value of suffixProperty) does not exist (Similar to defaultSuffix)
        ),
        // the transform function, e.g. 
        // 👇 `love.forte.plugin.suspendtrans.runtime.$runInBlocking$`
        // it will be like 
        // ```
        // @JvmBlocking suspend fun runXxx() { ... }
        // fun runXxxBlocking() = `$runInBlocking$` { runXxx() /* suspend  */ } // generated function
        // ```
        transformFunctionInfo = FunctionInfo(
            packageName = "love.forte.plugin.suspendtrans.runtime",
            className = null, // null if top-level function
            functionName = "\$runInBlocking\$"
        ),
        transformReturnType = null, // return type, or null if return the return type of origin function, e.g. `ClassInfo("java.util.concurrent", "CompletableFuture")`
        transformReturnTypeGeneric = false, // if you return like `CompletableFuture<T>`, make it `true`
        originFunctionIncludeAnnotations = listOf(IncludeAnnotation(ClassInfo("kotlin.jvm", "JvmSynthetic"))), // include into origin function
        copyAnnotationsToSyntheticFunction = true,
        copyAnnotationExcludes = listOf(ClassInfo("kotlin.jvm", "JvmSynthetic")), // do not copy from origin function
        syntheticFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmApi4JAnnotationClassInfo)) // include into synthetic function
    )

    addJvmTransformers(
        customJvmTransformer, ...
    )

    // or addJsTransformers(...)

}
```

举个例子，你想要使用一个单独的注解就完成`@JvmAsync`, `@JvmBlocking`, and `@JsPromise`的工作：


```kotlin
// 你在JVM平台上的转化函数
// 比如 com.example.Transforms.jvm.kt

@Deprecated("Just used by compiler", level = DeprecationLevel.HIDDEN)
fun <T> runInBlocking(block: suspend () -> T): T {
    // run the `block` in blocking
    runBlocking { block() }
}

@Deprecated("Just used by compiler", level = DeprecationLevel.HIDDEN)
public fun <T> runInAsync(block: suspend () -> T, scope: CoroutineScope? = null): CompletableFuture<T> {
    // run the `block` in async
    val scope0 = scope ?: GlobalScope
    return scope0.future { block() }
    
    /*
        `scope` 是 `block`'s 所处的容器:
        ```
        interface Container {
            @JvmAsync
            suspend fun run()
            👇 compiled
            
            fun runAsync() = runInAsync(block = { run() }, scope = this as? CoroutineScope)
        }
        ``` 
     */
}

// 你JS平台上的转化函数
// 比如 com.example.Transforms.js.kt
@Deprecated("Just used by compiler", level = DeprecationLevel.HIDDEN)
fun <T> runInPromise(block: suspend () -> T, scope: CoroutineScope? = null): T {
    val scope0 = scope ?: GlobalScope
    return scope0.promise { block() }
}
```

创建你的注解：

```kotlin
// Your single annotation
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class SuspendTrans(
    val blockingBaseName: String = "",
    val blockingSuffix: String = "Blocking",
    val blockingAsProperty: Boolean = false,

    val asyncBaseName: String = "",
    val asyncSuffix: String = "Async",
    val asyncAsProperty: Boolean = false,

    val jsPromiseBaseName: String = "",
    val jsPromiseSuffix: String = "Async",
    val jsPromiseAsProperty: Boolean = false,
)
```

然后，配置你的构建脚本

```kotlin
// The annotation type
val suspendTransMarkAnnotationClassInfo = ClassInfo("love.forte.simbot.suspendrunner", "SuspendTrans")

// The mark annotations
val jvmSuspendTransMarkAnnotationForBlocking = MarkAnnotation(
    suspendTransMarkAnnotationClassInfo,
    baseNameProperty = "blockingBaseName",
    suffixProperty = "blockingSuffix",
    asPropertyProperty = "blockingAsProperty",
    defaultSuffix = "Blocking",
)
val jvmSuspendTransMarkAnnotationForAsync = MarkAnnotation(
    suspendTransMarkAnnotationClassInfo,
    baseNameProperty = "asyncBaseName",
    suffixProperty = "asyncSuffix",
    asPropertyProperty = "asyncAsProperty",
    defaultSuffix = "Async",
)
val jsSuspendTransMarkAnnotationForPromise = MarkAnnotation(
    suspendTransMarkAnnotationClassInfo,
    baseNameProperty = "jsPromiseBaseName",
    suffixProperty = "jsPromiseSuffix",
    asPropertyProperty = "jsPromiseAsProperty",
    defaultSuffix = "Async",
)

// The transform functions
val jvmBlockingFunction = FunctionInfo("com.example", null, "runInBlocking")
val jvmAsyncFunction = FunctionInfo("com.example", null, "runInAsync")
val jsPromiseFunction = FunctionInfo("com.example", null, "runInPromise")

// The transformers
val suspendTransTransformerForJvmBlocking: Transformer = Transformer(
    markAnnotation = jvmSuspendTransMarkAnnotationForBlocking,
    transformFunctionInfo = jvmBlockingFunction,
    transformReturnType = null, // same as origin function
    transformReturnTypeGeneric = false,
    // include @JvmSynthetic into origin function
    originFunctionIncludeAnnotations = listOf(
        SuspendTransformConfiguration.jvmSyntheticClassInfo,
    ),
    copyAnnotationsToSyntheticFunction = true,
    // excludes: @JvmSynthetic, @OptIn, @SuspendTrans
    copyAnnotationExcludes = listOf(
        SuspendTransformConfiguration.jvmSyntheticClassInfo,
        SuspendTransformConfiguration.kotlinOptInClassInfo,
        suspendTransMarkAnnotationClassInfo,
    ),
    // Include into synthetic function's annotations
    syntheticFunctionIncludeAnnotations = listOf()
)

val suspendTransTransformerForJvmAsync: Transformer = Transformer(
    markAnnotation = jvmSuspendTransMarkAnnotationForAsync,
    transformFunctionInfo = jvmAsyncFunction,
    transformReturnType = ClassInfo("java.util.concurrent", "CompletableFuture"),
    transformReturnTypeGeneric = true, // Future's generic type is origin function's return type.
    // include @JvmSynthetic into origin function
    originFunctionIncludeAnnotations = listOf(
        SuspendTransformConfiguration.jvmSyntheticClassInfo,
    ),
    copyAnnotationsToSyntheticFunction = true,
    // excludes: @JvmSynthetic, @OptIn, @SuspendTrans
    copyAnnotationExcludes = listOf(
        SuspendTransformConfiguration.jvmSyntheticClassInfo,
        suspendTransMarkAnnotationClassInfo,
        SuspendTransformConfiguration.kotlinOptInClassInfo,
    ),
    // Include into synthetic function's annotations
    syntheticFunctionIncludeAnnotations = listOf()
)

val suspendTransTransformerForJsPromise: Transformer = Transformer(
    markAnnotation = jsSuspendTransMarkAnnotationForPromise,
    transformFunctionInfo = jsPromiseFunction,
    transformReturnType = ClassInfo("kotlin.js", "Promise"),
    transformReturnTypeGeneric = true, // Promise's generic type is origin function's return type.
    originFunctionIncludeAnnotations = listOf(),
    copyAnnotationsToSyntheticFunction = true,
    // excludes: @OptIn, @SuspendTrans
    copyAnnotationExcludes = listOf(
        SuspendTransformConfiguration.kotlinOptInClassInfo,
        suspendTransMarkAnnotationClassInfo,
    ),
    syntheticFunctionIncludeAnnotations = listOf()
)

// 上面这些东西也可以考虑在 `buildSrc` 中定义。

suspendTransform {
    // 关闭它们，并使用你自己自定义的 runtime 和 annotation
    includeRuntime = false     
    includeAnnotation = false
    // 注意：如果禁用 includeAnnotation, 你需要自定义 targetMarker 或将其设置为 `null`
    //  更多参考: https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/73
    targetMarker = null // 或自定义

    addJvmTransformers(
        suspendTransTransformerForJvmBlocking,
        suspendTransTransformerForJvmAsync
    )
    addJsTransformers(
        suspendTransTransformerForJsPromise
    )
}
```

## 应用案例

- [Simple Robot 框架](https://github.com/simple-robot/simpler-robot) (完全定制化)

## 开源协议

参考 [LICENSE](LICENSE) .

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
FITNESS FOR love.forte.plugin.suspendtrans.A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
