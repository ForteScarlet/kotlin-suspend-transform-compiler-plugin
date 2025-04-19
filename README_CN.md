# Kotlin suspend transform 编译器插件
[![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

<img src=".project/cover.png" alt="封面图">

[GitHub](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin) | [Gitee](https://gitee.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin)

**English** | [简体中文](README_CN.md)

## 概述

用于为挂起函数生成平台兼容函数的 Kotlin 编译器插件。

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
    // 对 Java 隐藏
    @JvmSynthetic
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    }
    @Api4J // 需要显式启用的注解，向 Kotlin 提供警告
    fun waitAndGetBlocking(): String = runInBlocking { waitAndGet() } // 'runInBlocking' 来自插件提供的运行时

    @Api4J // 需要显式启用的注解，向 Kotlin 提供警告
    fun waitAndGetAsync(): CompletableFuture<out String> = runInAsync { waitAndGet() } // 'runInAsync' 来自插件提供的运行时
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
    @Api4Js // 需要显式启用的注解，向 Kotlin 提供警告
    fun waitAndGetAsync(): Promise<String> = runInAsync { waitAndGet() } // 'runInAsync' 来自插件提供的运行时
}
```

> ~~JS 平台目标暂未支持。参见：[KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)~~
>
> 自 0.6.0 版本起已支持 JS！进展见 [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)，最终实现见 [#39](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/pull/39)！

### WasmJS

> [!warning]
> 自 `v0.6.0` 起处于实验阶段，不成熟且不稳定

```kotlin
class Foo {
    @JsPromise
    suspend fun waitAndGet(): String {
        delay(5)
        return "Hello"
    } 
}

// 由**你**自定义的部分函数或类型...
// 这些不包含在运行时中。
// 由于 WasmJS 对各类使用存在诸多限制...
// 目前尚未找到完美处理方式。
// 在此之前，你可以自定义函数和类型来控制编译器插件的行为，
// 就像对其他平台所做的那样。

fun <T> runInAsync(block: suspend () -> T): AsyncResult<T> = AsyncResult(block)

class AsyncResult<T>(val block: suspend () -> T) {
    @OptIn(DelicateCoroutinesApi::class)
    fun toPromise(): Promise<JsAny?> {
        return GlobalScope.promise { block() }
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
    @Api4Js // 需要显式启用的注解，向 Kotlin 提供警告
    fun waitAndGetAsync(): AsyncResult<String> = runInAsync { waitAndGet() } // 'runInAsync' 来自插件提供的运行时
    // AsyncResult 是**你**自定义的类型
}
```

## 使用方式

### 版本说明

`0.9.0` 及之前版本使用 `x.y.z` 的命名规则。但由于 Kotlin 编译器可能随版本变化，
这种命名方式无法反映对应的 Kotlin 版本，可能导致混淆。

因此，`0.9.0` 之后的版本将采用 `$Kotlin-$plugin` 的命名形式，
例如 `2.0.20-0.9.1`。前半部分为构建所用的 Kotlin 版本，后半部分为插件版本。

若版本小于等于 `0.9.0`，可参考以下对照表：

| Kotlin 版本 | 插件版本                    |
|-----------|-------------------------|
| `2.0.0`   | `0.8.0-beta1` ~ `0.9.0` |
| `1.9.22`  | `0.7.0-beta1`           |
| `1.9.21`  | `0.6.0`                 |
| `1.9.10`  | `0.5.1`                 |
| `1.9.0`   | `0.5.0`                 |
| `1.8.21`  | `0.3.1` ~ `0.4.0`       |

> [!note]
> 未详细记录各 Kotlin 版本的编译器插件兼容性。
> 根据经验，次要版本升级（如 `1.8.0` -> `1.9.0`）更可能不兼容，
> 补丁版本（如 `1.9.21` -> `1.9.22`）不兼容概率较低。

### Gradle

**使用 [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block)：**

_build.gradle.kts_

```Kotlin
plugins {
    kotlin("jvm") version "$KOTLIN_VERSION" // 或 multiplatform
    id("love.forte.plugin.suspend-transform") version "$PLUGIN_VERSION" 
    // 其他...
}

// 其他...

// 配置插件
suspendTransformPlugin {
    // 配置 SuspendTransformPluginExtension ...
}
```

**使用 [传统插件应用方式](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application)：**

_build.gradle.kts_

```Kotlin
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
    id("org.jetbrains.kotlin.jvm") // 或 multiplatform?
    id("love.forte.plugin.suspend-transform") 
    // 其他...
}

// 其他...

// 配置插件
suspendTransformPlugin {
    // 配置 SuspendTransformPluginExtension ...
}
```

## 配置扩展

### 启用插件

启用 Kotlin 编译器插件。默认值为 `true`。

```Kotlin
suspendTransformPlugin {
    enabled = true
}
```

### 包含默认注解和运行时

若需使用我们提供的转换器，需添加 `annotation` 和 `runtime` 依赖。
可通过配置自动添加：

```Kotlin
suspendTransformPlugin {
    // 包含注解
    // 默认为 `true`
    includeAnnotation = true
    // 默认值可留空，使用专属默认值
    annotationDependency {
        // 默认为 `compileOnly`
        configurationName = "compileOnly"
        // 默认与插件版本相同
        version = "<ANNOTATION_VERSION>"
    }
    
    // 包含运行时
    // 默认为 `true`
    includeRuntime = true
    // 默认值可留空，使用专属默认值
    runtimeDependency {
        // 默认为 `implementation`
        configurationName = "implementation"
        // 默认与插件版本相同
        version = "<RUNTIME_VERSION>"
    }
}
```

也可手动添加依赖：

```Kotlin
plugin {
    kotlin("jvm") version "..." // 以 Kotlin/JVM 为例
    id("love.forte.plugin.suspend-transform") version "2.1.20-0.12.0"
}

dependencies {
    // 注解
    compileOnly("love.forte.plugin.suspend-transform:suspend-transform-annotation:<VERSION>")
    // 运行时
    implementation("love.forte.plugin.suspend-transform:suspend-transform-runtime:<VERSION>")
}

suspendTransformPlugin {
    // 禁用自动包含
    includeAnnotation = false
    includeRuntime = false
}
```

### 添加转换器

`Transformer` 用于描述如何转换挂起函数。需添加 `Transformer` 以使插件生效。

```Kotlin
suspendTransformPlugin {
    // 配置转换器
    transformers {
        add(TargetPlatform.JVM) { // this: TransformerSpec
            // 配置 TransformerSpec...
        }

        addJvm { // this: TransformerSpec
            // 配置 TransformerSpec...
        }

        // 使用预置的默认转换器
        add(TargetPlatform.JVM, SuspendTransformConfigurations.jvmBlockingTransformer)

        addJvm { // this: TransformerSpec
            // 基于现有转换器调整
            from(SuspendTransformConfigurations.jvmBlockingTransformer)
            // 进一步配置...
        }
    }
}
```

#### 添加默认转换器

我们提供了一些常用实现，可通过配置快速使用。

> [!note]
> 默认 `Transformer` 依赖我们提供的 `annotation` 和 `runtime`，请确保已包含。

**JVM 阻塞式**

```Kotlin
suspendTransformPlugin {
    transformers {
        // 方式一：
        addJvmBlocking()

        // 方式二：
        addJvm(SuspendTransformConfigurations.jvmBlockingTransformer)
    }
}
```

`JvmBlocking` 允许在挂起函数上标记 `@JvmBlocking`，生成 `xxxBlocking` 函数。

```Kotlin
class Cat {
    @JvmBlocking
    suspend fun meow() {
        // ...
    }
    
    // 生成：
    fun meowBlocking() {
        `$runInBlocking$` { meow() }
    }
}
```

`$runInBlocking$` 基于 `kotlinx.coroutines.runBlocking`。

**JVM 异步式**

```Kotlin
suspendTransformPlugin {
    transformers {
        // 方式一：
        addJvmAsync()

        // 方式二：
        addJvm(SuspendTransformConfigurations.jvmAsyncTransformer)
    }
}
```

`JvmAsync` 允许在挂起函数上标记 `@JvmAsync`，生成 `xxxAsync` 函数。

```Kotlin
class Cat {
    @JvmBlocking
    suspend fun meow(): String = "Meow!"
    
    // 生成：
    fun meowAsync(): CompletableFuture<out String> {
        `$runInAsync$`(block = { meow() }, scope = this as? CoroutineScope)
    }
}
```

`block` 是需要执行的原始挂起函数，`scope` 是使用的协程作用域。

若当前作用域是 `CoroutineScope`，则优先使用自身。否则内部使用 `GlobalScope`。

使用 `GlobalScope` 的原因：
1. 全局性。
2. 不可见，不会被手动关闭。
3. 不涉及 IO，无需自定义调度器。

若有异议，欢迎提交 issue！

**JS Promise**

```Kotlin
suspendTransformPlugin {
    transformers {
        // 方式一：
        addJsPromise()

        // 方式二：
        addJs(SuspendTransformConfigurations.jsPromiseTransformer)
    }
}
```

```Kotlin
class Cat {
    @JsPromise
    suspend fun meow(): String = "Meow!"
    
    // 生成：
    fun meowAsync(): Promise<String> {
        `$runInAsync$`(block = { meow() }, scope = this as? CoroutineScope)
    }
}
```

#### 使用默认转换器

`addJvmBlocking()` 和 `addJvmAsync()` 可以被合并为 `useJvmDefault()`。

```Kotlin
suspendTransformPlugin {
    transformers {
        // 包括 addJvmBlocking() 和 addJvmAsync()
        useJvmDefault()
    }
}
```

`addJsPromise()` 可以被合并为 `useJsDefault()` 。

```Kotlin
suspendTransformPlugin {
    transformers {
        // 包括 addJsPromise()
        useJsDefault()
    }
}
```

`useJvmDefault()` 和 `useJsDefault` 可以被合并为 `useDefault()` 。

```Kotlin
suspendTransformPlugin {
    transformers {
        // 包括 addJvmDefault() 和 addJsPromise()
        useDefault()
    }
}
```

#### 自定义转换器

若默认转换器不满足需求，可自定义 `Transformer`，例如完全自定义阻塞逻辑。

> 完整自定义实现参考：
> https://github.com/simple-robot/simpler-robot/blob/v4-main/simbot-commons/simbot-common-suspend-runner/src/jvmMain/kotlin/love/forte/simbot/suspendrunner/BlockingRunner.kt

```Kotlin
suspendTransformPlugin {
    // 自定义时可能无需默认注解和运行时
    includeAnnotation = false
    includeRuntime = false
    
    transformer {
        // 具体配置见下文
    }
}
```

示例：自定义注解 `@JBlock`，通过函数 `inBlock` 执行挂起函数。

```Kotlin
// 自定义注解
annotation class JBlock(...)

// 自定义顶层转换函数
fun <T> inBlock(block: suspend () -> T): T {
    TODO("你的实现")
}
```

假设注解包含以下属性：
- `baseName`: 生成函数的基础名（默认为原函数名）
- `suffix`: 生成函数名的后缀
- `asProperty`: 将生成函数转为属性（适用于无参数的函数）

注解定义：

```Kotlin
annotation class JBlock(
    val baseName: String = "",
    val suffix: String = "Blocking",
    val asProperty: Boolean = false
)
```

配置示例：

```Kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
                // 注解类信息
                classInfo {
                    packageName = "com.example"
                    className = "JBlock"
                }

                // 属性名映射
                baseNameProperty = "baseName"  // 默认为 `baseName`
                suffixProperty = "suffix"      // 默认为 `suffix`
                asPropertyProperty = "asProperty" // 默认为 `asProperty`

                // 默认值需手动配置（编译器无法获取注解默认值）
                defaultSuffix = "Blocking" 
                defaultAsProperty = false 
            }
        }
    }
}
```

若属性名不同：

```Kotlin
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)
```

配置调整：

```Kotlin
baseNameProperty = "myBaseName"
suffixProperty = "mySuffix"
asPropertyProperty = "myAsProperty"
```

转换函数配置：

```Kotlin
transformFunctionInfo {
    packageName = "com.example"
    functionName = "inBlock"
}

// 返回类型配置
transformReturnType = null // 与原函数返回类型相同
transformReturnTypeGeneric = false // 无泛型
```

注解复制配置示例：

```Kotlin
addOriginFunctionIncludeAnnotation {
  classInfo {
    packageName = "kotlin.jvm"
    className = "JvmSynthetic"
  }
  repeatable = false
}

addSyntheticFunctionIncludeAnnotation {
  classInfo {
    packageName = "com.example"
    className = "JApi"
  }
  includeProperty = true
}

addCopyAnnotationExclude {
  from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
}
```

完整示例：

代码：

```Kotlin
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)

@RequiresOptIn(message = "Java 接口", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class JApi

fun <T> inBlock(block: suspend () -> T): T {
  TODO("你的实现")
}
```

配置：

```Kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
              classInfo {
                packageName = "com.example"
                className = "JBlock"
              }

              baseNameProperty = "myBaseName"
              suffixProperty = "mySuffix"
              asPropertyProperty = "myAsProperty"

              defaultSuffix = "Blocking"
              defaultAsProperty = false
            }
          
            transformFunctionInfo {
              packageName = "com.example"
              functionName = "inBlock"
            }
          
            copyAnnotationsToSyntheticFunction = true
            copyAnnotationsToSyntheticProperty = true

            addOriginFunctionIncludeAnnotation {
              classInfo.from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
              repeatable = false
            }

            addSyntheticFunctionIncludeAnnotation {
              classInfo {
                packageName = "com.example"
                className = "JApi"
              }
              includeProperty = true
            }

            addCopyAnnotationExclude {
              from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
            }
        }
    }
}
```

> [!note]
> 同一注解可通过不同属性名复用于多个转换器。例如：
> ```Kotlin
> annotation class JTrans(
>     val blockingBaseName: String = "",
>     val blockingSuffix: String = "Blocking",
>     val blockingAsProperty: Boolean = false,
>     
>     val asyncBaseName: String = "",
>     val asyncSuffix: String = "Async",
>     val asyncAsProperty: Boolean = false
> )
> ```

## 注意事项
### Gradle JVM

**Gradle JVM** 必须为 JDK11+

### K2

自 `v0.7.0` 起支持 K2。

### JsExport

若需在 JS 中使用 `@JsExport` 的默认配置：

_build.gradle.kts_

```kotlin
plugins {
    // ...
}

suspendTransformPlugin {
  transformers {
    addJsPromise {
      addCopyAnnotationExclude {
        // 生成函数不包含 `@JsExport.Ignore`
        from(kotlinJsExportIgnoreClassInfo)
      }
    }
  }
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

## 效果示例

**源码:**

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
    @JvmBlocking(asProperty = true) // 必须为 'asProperty=true'
    override suspend fun self(): FooImpl = this
}

class Bar {
    @JvmBlocking
    @JvmAsync
    suspend fun bar(): String = ""

    suspend fun noTrans(): Int = 1
}
```

**编译结果（简化版）:**

```kotlin
// 生成代码的详细实现略，参见原文
```

## 应用案例

- [Simple Robot Frameworks](https://github.com/simple-robot/simpler-robot) (完全自定义实现)


## 许可证

见 [LICENSE](LICENSE) 。

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
