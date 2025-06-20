# Kotlin suspend transform compiler plugin
[![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/) 
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

<img src=".project/cover.png" alt="cover">

[GitHub](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin) | [Gitee](https://gitee.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin)

**English** | [简体中文](README_CN.md)

## Summary

Kotlin compiler plugin for generating platform-compatible functions for suspend functions.

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

## Usage

### The version

Before `0.9.0` (included), the naming convention for versions was `x.y.z`. 
But it seems that the contents of the Kotlin compiler may find changes with each version, 
and such version numbers do not reflect the corresponding Kotlin version, 
and may lead to some confusion as a result.

Therefore, starting after `0.9.0`, versions will be named in the form `$Kotlin-$plugin`, 
e.g. `2.0.20-0.9.1`. 
The first half is the version of Kotlin used for the build, while the second half is the version of this plugin.

If the version is less than or equal to `0.9.0`, you can refer to this comparison table:

| Kotlin version | plugin version          |
|----------------|-------------------------|
| `2.0.0`        | `0.8.0-beta1` ~ `0.9.0` |
| `1.9.22`       | `0.7.0-beta1`           |
| `1.9.21`       | `0.6.0`                 |
| `1.9.10`       | `0.5.1`                 |
| `1.9.0`        | `0.5.0`                 |
| `1.8.21`       | `0.3.1` ~ `0.4.0`       |

> [!note]
> I haven't documented in detail the compiler plugin compatibility between each Kotlin version.
> From my memory and guess, Kotlin versions have a higher probability of incompatibility when minor is added (e.g. `1.8.0` -> `1.9.0`), 
> and a smaller probability of incompatibility when patch is added (e.g. `1.9.21` -> `1.9.22`).

### Gradle

**Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):**

_build.gradle.kts_

```Kotlin
plugins {
    kotlin("jvm") version "$KOTLIN_VERSION" // or multiplatform
    id("love.forte.plugin.suspend-transform") version "$PLUGIN_VERSION" 
    // other...
}

// other...

// config it.
suspendTransformPlugin {
    // Config the SuspendTransformPluginExtension ...
}
```

**Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):**

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
    id("org.jetbrains.kotlin.jvm") // or multiplatform?
    id("love.forte.plugin.suspend-transform") 
    // other...
}

// other...

// config it.
suspendTransformPlugin {
    // Config the SuspendTransformPluginExtension ...
}
```

## Config the extension

### Enabled

Enable the Kotlin compiler plugin.
Default value is `true`.

```Kotlin
suspendTransformPlugin {
    enabled = true
}
```

### Include the default annotations and runtime

If you wish to use the Transformer we provide, then you may need to add the `annotation` and `runtime` dependencies.

You can add them automatically via configuration.

```Kotlin
suspendTransformPlugin {
    // include the annotation
    // Default is `true`
    includeAnnotation = true
    // The default can be left unconfigured and the default values are used exclusively.
    annotationDependency {
        // Default is `compileOnly`
        configurationName = "compileOnly"
        // Default is same as the plugin version
        version = "<ANNOTATION_VERSION>"
    }
    
    // Include the runtime
    // Default is `true`
    includeRuntime = true
    // The default can be left unconfigured and the default values are used exclusively.
    runtimeDependency {
        // Default is `implementation`
        configurationName = "implementation"
        // Default is same as the plugin version
        version = "<RUNTIME_VERSION>"
    }
}
```

You can also disable them and add dependencies manually.

```Kotlin
plugin {
    kotlin("jvm") version "..." // Take the Kotlin/JVM as an example
    id("love.forte.plugin.suspend-transform") version "<VERSION>"
}

dependencies {
    // annotation
    compileOnly("love.forte.plugin.suspend-transform:suspend-transform-annotation:<VERSION>")
    // runtime
    implementation("love.forte.plugin.suspend-transform:suspend-transform-runtime:<VERSION>")
}

suspendTransformPlugin {
    // Disable them
    includeAnnotation = false
    includeRuntime = false
}
```

### Add transformers

`Transformer` is the type used to describe how the suspend function is transformed. 
You need to add some `Transformer`s to make the compiler plugin actually work.


```Kotlin
suspendTransformPlugin {
    // Config the transformers
    transformers {
        add(TargetPlatform.JVM) { // this: TransformerSpec
            // Config the TransformerSpec...
        }

        addJvm { // this: TransformerSpec
            // Config the TransformerSpec...
        }

        // Use a default transformer we provided from `SuspendTransformConfigurations`
        add(TargetPlatform.JVM, SuspendTransformConfigurations.jvmBlockingTransformer)

        addJvm { // this: TransformerSpec
            // Modify and adjust from a Transformer
            from(SuspendTransformConfigurations.jvmBlockingTransformer)
            // Further configurations...
        }
    }
}
```

#### Add the default transformers

First, we provide some simple and commonly used implementations.
You can use them simply and quickly through configuration.

> [!note]
> The default `Transformer`s depend on the `annotation` and `runtime` we provide. 
> Make sure you include them before using it.

**JVM blocking**

```Kotlin
suspendTransformPlugin {
    transformers {
        // The 1st way:
        addJvmBlocking()

        // Or the 2ed way:
        addJvm(SuspendTransformConfigurations.jvmBlockingTransformer)
        // Or use transformers.add(TargetPlatform.JVM, jvmBlockingTransformer), etc.
    }
}
```

`JvmBlocking` allows you to mark `@JvmBlocking` on the suspend function, 
which generates a `xxxBlocking` function.

```Kotlin
class Cat {
    @JvmBlocking
    suspend fun meow() {
        // ...
    }
    
    // Generated:
    fun meowBlocking() {
        `$runInBlocking$` { meow() }
    }
}
```

The `$runInBlocking$` based on `kotlinx.coroutines.runBlocking` 。

**JVM Async**

```Kotlin
suspendTransformPlugin {
    transformers {
        // The 1st way:
        addJvmAsync()

        // Or the 2ed way:
        addJvm(SuspendTransformConfigurations.jvmAsyncTransformer)
        // Or use transformers.add(TargetPlatform.JVM, jvmAsyncTransformer), etc.
    }
}
```

`JvmAsync` allows you to mark `@JvmAsync` on the suspend function,
which generates a `xxxAsync` function.

```Kotlin
class Cat {
    @JvmBlocking
    suspend fun meow(): String = "Meow!"
    
    // Generated:
    fun meowAsync(): CompletableFuture<out String> {
        `$runInAsync$`(block = { meow() }, scope = this as? CoroutineScope)
    }
}
```

The `block` is the original suspend function that needs to be executed 
and the `scope` is the `CoroutineScope` that will be used.

If the current scope is a `CoroutineScope`, it takes precedence over itself.
Otherwise, `GlobalScope` is used internally.

Why use `GlobalScope`: When using an internal scope, this scope qualifies:
1. global.
2. is never visible externally, so it is not artificially closed.
3. is not intended for IO and does not require a custom dispatcher.

We believe `GlobalScope` meets these conditions.

_Have a different point? Feel free to create issue!_

**JS Promise**

```Kotlin
suspendTransformPlugin {
    transformers {
        // The 1st way:
        addJsPromise()

        // Or the 2ed way:
        addJs(SuspendTransformConfigurations.jsPromiseTransformer)
        // Or use transformers.add(TargetPlatform.JS, jsPromiseTransformer), etc.
    }
}
```

```Kotlin
class Cat {
    @JsPromise
    suspend fun meow(): String = "Meow!"
    
    // Generated:
    fun meowAsync(): Promise<String> {
        `$runInAsync$`(block = { meow() }, scope = this as? CoroutineScope)
    }
}
```

The `block` is the original suspend function that needs to be executed
and the `scope` is the `CoroutineScope` that will be used.

#### Use the defaults

The `addJvmBlocking()` and `addJvmAsync()` may be combined as `useJvmDefault()`.

```Kotlin
suspendTransformPlugin {
    transformers {
        // Includes addJvmBlocking() and addJvmAsync()
        useJvmDefault()
    }
}
```

The `addJsPromise()` may be combined as `useJsDefault()`.

```Kotlin
suspendTransformPlugin {
    transformers {
        // Includes addJsPromise()
        useJsDefault()
    }
}
```

The `useJvmDefault()` and `useJsDefault()` may be combined as `useDefault()`.

```Kotlin
suspendTransformPlugin {
    transformers {
        // Includes useJvmDefault() and useJsDefault()
        useDefault()
    }
}
```

#### Use custom transformers

You can also customize your `Transformer` if the default `Transformer`s don't meet your needs, 
e.g. if you want to fully implement blocking logic and don't want to use `kotlinx.coroutines.runBlocking`.

> A fully customized implementation of JVM Blocking/Async Transformers reference: 
> https://github.com/simple-robot/simpler-robot/blob/v4-main/simbot-commons/simbot-common-suspend-runner/src/jvmMain/kotlin/love/forte/simbot/suspendrunner/BlockingRunner.kt

```Kotlin
suspendTransformPlugin {
    // If customized, then you may not use the annotation and runtime we provide.
    includeAnnotation = false
    includeRuntime = false
    
    transformer {
        // See below for details
    }
}
```

As an example, you intend to create a custom annotation: `@JBlock`, 
which is executed via the function `inBlock` when the suspend function uses this annotation.

```Kotlin
// Your annotation
annotation class JBlock(...)

// Your top-level transform function
fun <T> inBlock(block: suspend () -> T): T {
    TODO("Your impl")
}
```

First, let's agree that the following properties should be included in the annotation:

- `baseName`: The generated function's **base name**. 
  When the value of this property is empty, the name of the original function is used by default.
  ```Kotlin
  @JBlock(baseName = "")
  suspend fun meow1() // Generated function name: ${baseName}${suffix} -> meow1Blocking
  
  @JBlock(baseName = "meow999")
  suspend fun meow2() // Generated function name: ${baseName}${suffix} -> meow999Blocking
  ```
- `suffix`: The generated function name's suffix.
- `asProperty`: Make the generated function a property. 
  Can be used in cases where the original function has no arguments.
  ```Kotlin
  @JBlock(asProperty = true)
  suspend fun value(): Int
  
  // Generated:
  val valueBlocking: Int
      get() = inBlock { value() }
  ```

So your annotation should look like this:

```Kotlin
annotation class JBlock(
    val baseName: String = "",
    val suffix: String = "Blocking",
    val asProperty: Boolean = false
)
```

The configuration:

```Kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
                // Your annotation class's info.
                classInfo {
                    packageName = "com.example"
                    className = "JBlock"
                }

                // The property names.
                baseNameProperty = "baseName"  // Default is `baseName`
                suffixProperty = "suffix"      // Default is `suffix`
                asPropertyProperty = "asProperty" // Default is `asProperty`

                // The compiler plugin doesn't seem to be able to get the default values for annotations 
                // (or I haven't found a way to do it yet). 
                // So here you need to configure the default value of the annotation, which needs to be consistent with your definition.
                defaultSuffix = "Blocking" 
                defaultAsProperty = false  // For the same reasons as above. 
            }
        }
    }
}
```

However, the property names do not have to be the same as these three, as long as the function and type correspond. So we can adjust it like this:

```Kotlin
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)
```

The configuration:

```Kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
                // Your annotation class's info.
                classInfo {
                    packageName = "com.example"
                    className = "JBlock"
                }

                // The property names.
                baseNameProperty = "myBaseName"
                suffixProperty = "mySuffix"
                asPropertyProperty = "myAsProperty"
                
                // The default values.
                defaultSuffix = "Blocking" 
                defaultAsProperty = false 
            }
        }
    }
}
```

Then configure the information for your transform function.

```Kotlin
// Your top-level transform function
fun <T> inBlock(block: suspend () -> T): T {
    TODO("Your impl")
}
```

The configuration:

```Kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
                // ...
            }

            // The function info
            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inBlock"
            }

            // The return type configs

            // The return type.
            // If `null` it means the same type as the original function return.
            // If you return a specific type (e.g. `CompletableFuture`) you need to configure that type.
            // 
            // Default value is null.
            transformReturnType = null

            // Whether the returned type contains a generic type that is of the same type as the original function.
            // e.g. CompletableFuture<T>, The `T` represents the value returned by the original function.
            // In this case it is set to `true`.
            //
            // Set to `false` if the return type is of a specific type, 
            // but without a generic (a rare case, an example: `Job`).
            // Valid if `transformReturnType` is not null.
            // 
            // Default value is false.
            transformReturnTypeGeneric = false
        }
    }
}
```

Finally, in the process of generating the function, we allow some manipulation of the annotations.
- Copy annotations from original function to generated synthetic function.
  - exclude some annotations from copying.
- Include some annotations to original function.
- Include some annotations to generated synthetic function.

Now let's assume:
- We want to add `@JvmSynthetic` to the original function.
- We want to add `@JApi` to the generated synthetic function.
- Copy the annotations without copying `@JvmSynthetic` (exclude `@JvmSynthetic`).

The `@JApi`:

```Kotlin
@RequiresOptIn(message = "Api for Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class JApi
```

The configuration:

```Kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
                // ...
            }
            transformFunctionInfo {
                // ...
            }
          
            // Enabling annotated copies
            // Default is FALSE
            copyAnnotationsToSyntheticFunction = true
            // If the generated synthetic function is property (asProperty=true),
            // Copy annotations to the property. 
            // Otherwise, copy to the property's getter function.
            // Default is FALSE
            copyAnnotationsToSyntheticProperty = true

            // Include `@kotlin.jvm.JvmSynthetic` to original function.
            addOriginFunctionIncludeAnnotation {
              // Some common types are defined in SuspendTransformConfigurations. See below.
              classInfo {
                packageName = "kotlin.jvm"
                className = "JvmSynthetic"
              }
              // Default is false
              repeatable = false
            }

            // Include `@com.example.JApi` to generated synthetic function
            addSyntheticFunctionIncludeAnnotation {
              classInfo {
                packageName = "com.example"
                className = "JApi"
              }
              // Marks whether this annotation supports being added to a property.
              // Default is FALSE
              includeProperty = true
            }

            // Exclude `@kotlin.jvm.JvmSynthetic` when copying.
            addCopyAnnotationExclude {
              // SuspendTransformConfigurations provides a small number of
              // common annotations or type definitions that can be used directly.
              from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
            }
        }
    }
}
```

The full example:

Code:

```Kotlin
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)

@RequiresOptIn(message = "Api for Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class JApi

fun <T> inBlock(block: suspend () -> T): T {
  TODO("Your impl")
}
```

Configuration:

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
              classInfo {
                from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
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
        }
    }
}
```

> [!note]
> Since the property name is configurable, the same annotation can be reused on multiple transformers.
> Annotation:
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
> Configuration:
> ```Kotlin
> suspendTransformPlugin {
>    includeAnnotation = false
>    includeRuntime = false
>    transformers {
>        // For blocking
>        addJvm {
>            markAnnotation {
>                classInfo {
>                    packageName = "com.example"
>                    className = "JTrans"
>                }
>                baseNameProperty = "blockingBaseName"
>                suffixProperty = "blockingSuffix"
>                asPropertyProperty = "blockingAsProperty"
>                defaultSuffix = "Blocking"
>                defaultAsProperty = false
>            }
>
>            transformFunctionInfo {
>                packageName = "com.example"
>                functionName = "inBlock"
>            }
>
>            // other config...
>        }
>
>        // For async
>        addJvm {
>            markAnnotation {
>                classInfo {
>                    packageName = "com.example"
>                    className = "JTrans"
>                }
>                baseNameProperty = "asyncBaseName"
>                suffixProperty = "asyncSuffix"
>                asPropertyProperty = "asyncAsProperty"
>                defaultSuffix = "Async"
>                defaultAsProperty = false
>            }
>
>            transformFunctionInfo {
>                packageName = "com.example"
>                functionName = "inAsync"
>            }
>        }
>    }
>}
> ```

## Cautions
### Gradle JVM

**Gradle JVM** must be JDK11+

### K2

K2 is supported since `v0.7.0`.

### JsExport

If you want to use `@JsExport` with default configuration in JS,
try this:

_build.gradle.kts_

```kotlin
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations

plugins {
    // ...
}

suspendTransformPlugin {
  transformers {
    addJsPromise {
      addCopyAnnotationExclude {
        // The generated function does not include (copy) `@JsExport.Ignore`.
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

## Effect

**source:**

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
    @JvmBlocking(asProperty = true) // must be 'asProperty=true'
    override suspend fun self(): FooImpl = this
}

class Bar {
    @JvmBlocking
    @JvmAsync
    suspend fun bar(): String = ""

    suspend fun noTrans(): Int = 1
}
```

**compiled:**

> _Simplified from decompiled results._

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
