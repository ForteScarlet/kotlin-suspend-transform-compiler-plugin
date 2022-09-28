# Kotlin suspend transform compiler plugin
[![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/) 
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

<img src=".project/cover.png" alt="封面">

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

> JS 目标平台暂不支持。原因参考: [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)

## 使用
### Gradle

**通过 [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block) 使用:**

<details open>
<summary>Kotlin</summary>

_build.gradle.kts_

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "KOTLIN_VERSION" // 或 js? 或 multiplatform?
    id("love.forte.plugin.suspend-transform") version "$PLUGIN_VERSION" 
    // 其他...
}

// 其他...

// 配置
suspendTransform {
    enabled = true // 默认: true
    includeRuntime = true // 默认: true
    jvm {
        // ...
    }
    js {
        // ...
    }
}
```

</details>

<details>
<summary>Groovy</summary>

_build.gradle_

```groovy
plugins {
    id "org.jetbrains.kotlin.jvm" // 或 js? 或 multiplatform?
    id "love.forte.plugin.suspend-transform" version "$PLUGIN_VERSION" 
    // 其他...
}

// 其他...

// 配置
suspendTransform {
    enabled = true // 默认: true
    includeRuntime = true // 默认: true
    jvm {
        // ...
    }
    js {
        // ...
    }
}
```

</details>



**通过 [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application) 使用:**

<details open>
<summary>Kotlin</summary>

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
    enabled = true // 默认: true
    includeRuntime = true // 默认: true
    jvm {
        // ...
    }
    js {
        // ...
    }
}
```

</details>

<details>
<summary>Groovy</summary>

_build.gradle_

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:$VERSION"
    }
}



plugins {
    id "org.jetbrains.kotlin.jvm" // 或 js? 或 multiplatform?
    id "love.forte.plugin.suspend-transform" 
    // 其他...
}

// 其他...

// 配置
suspendTransform {
    enabled = true // 默认: true
    includeRuntime = true // 默认: true
    jvm {
        // ...
    }
    js {
        // ...
    }
}
```

</details>

### Maven

> 尚不支持。

## 注意事项

### Gradle JVM

Gradle JVM 必须满足 JDK11+

### JS平台

JS目标平台暂不支持。原因参考: [KT-53993](https://youtrack.jetbrains.com/issue/KT-53993)

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
    // 开启插件
    enabled = true
    // 添加依赖 'love.forte.plugin.suspend-transform:suspend-transform-runtime' 到运行时环境
    includeRuntime = true
    // 添加的依赖 'love.forte.plugin.suspend-transform:suspend-transform-runtime' 的 'configuration name'
    runtimeConfigurationName = "implementation"
    
    // jvm平台目标配置
    jvm {
        // jvm阻塞标记注解。默认: @JvmBlocking
        jvmBlockingMarkAnnotation.apply {
            annotationName = "love.forte.plugin.suspendtrans.annotation.JvmBlocking"
            baseNameProperty = "baseName"
            suffixProperty = "suffix"
            asPropertyProperty = "asProperty"
        }
        
        // jvm异步标记注解。默认: @JvmAsync
        jvmAsyncMarkAnnotation.apply {
            annotationName = "love.forte.plugin.suspendtrans.annotation.JvmAsync"
            baseNameProperty = "baseName"
            suffixProperty = "suffix"
            asPropertyProperty = "asProperty"
        }

        // jvm阻塞转化函数
        // 函数签名必须满足: fun <T> <fun-name>(block: suspend () -> T): T
        jvmBlockingFunctionName = "love.forte.plugin.suspendtrans.runtime.\$runInBlocking$"
        
        // jvm异步转化函数
        // 函数签名必须满足 fun <T> <fun-name>(block: suspend () -> T): CompletableFuture<T>
        jvmAsyncFunctionName = "love.forte.plugin.suspendtrans.runtime.\$runInAsync$"

        // 需要追加到生成的jvm阻塞函数上的额外注解
        syntheticBlockingFunctionIncludeAnnotations = listOf(
            SuspendTransformConfiguration.IncludeAnnotation("love.forte.plugin.suspendtrans.annotation.Api4J")
        )

        // 需要追加到生成的jvm异步函数上的额外注解
        syntheticAsyncFunctionIncludeAnnotations = listOf(
            SuspendTransformConfiguration.IncludeAnnotation("love.forte.plugin.suspendtrans.annotation.Api4J")
        )

        // 是否需要拷贝源函数上的注解到jvm阻塞函数上
        copyAnnotationsToSyntheticBlockingFunction = true
        
        // 是否需要拷贝源函数上的注解到jvm异步函数上
        copyAnnotationsToSyntheticAsyncFunction = true

        // 如果需要拷贝注解，配置拷贝过程中需要排除的注解
        copyAnnotationsToSyntheticBlockingFunctionExcludes = listOf(
            SuspendTransformConfiguration.ExcludeAnnotation("kotlin.jvm.JvmSynthetic")
        )

        // 如果需要拷贝注解，配置拷贝过程中需要排除的注解
        copyAnnotationsToSyntheticAsyncFunctionExcludes = listOf(
            SuspendTransformConfiguration.ExcludeAnnotation("kotlin.jvm.JvmSynthetic")
        )
    }
    
    js {
        // 与 'jvm' 中的配置基本类似
    }
    
    
}
```

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
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```