---
id: custom-transformers
title: 自定义转换器
sidebar_position: 3
---

import { VersionBadge } from '@site/src/components/Snippets';

本指南介绍当默认转换器不能满足您的特定需求时，如何创建自定义转换器。

## 概述

如果默认转换器不能满足您的需求，您可以创建完全自定义的转换器。
例如，如果您想在不使用 `kotlinx.coroutines.runBlocking` 的情况下实现阻塞逻辑，或者如果您需要平台特定的行为。

:::info
JVM 阻塞/异步转换器的完全自定义实现可以在以下位置找到：
[simbot's BlockingRunner](https://github.com/simple-robot/simpler-robot/blob/v4-main/simbot-commons/simbot-common-suspend-runner/src/jvmMain/kotlin/love/forte/simbot/suspendrunner/BlockingRunner.kt)
:::


:::warning 已知问题
自定义转换器函数不能放在使用编译器插件的同一模块中。它们需要
在单独的模块中创建。

更多信息：[#100](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues/100)
:::

## 基本设置

使用自定义转换器时，您通常不需要默认的注解和运行时依赖项：

```kotlin
suspendTransformPlugin {
    // 如果自定义，那么您可能不会使用我们提供的注解和运行时。
    includeAnnotation = false
    includeRuntime = false
    
    transformers {
        // 您的自定义转换器配置
    }
}
```

## 创建自定义转换器

让我们逐步创建一个自定义转换器。我们将创建一个使用自定义 `inBlock` 函数的 `@JBlock` 注解。

### 步骤 1：定义您的自定义组件

首先，定义您的自定义注解和转换函数：

```kotlin
// 您的自定义注解
annotation class JBlock(
    val baseName: String = "",
    val suffix: String = "Blocking",
    val asProperty: Boolean = false
)

// 您的自定义转换函数
fun <T> inBlock(block: suspend () -> T): T {
    // 您的自定义实现
    TODO("Your impl")
}
```

:::tip 约定
根据约定：转换函数的**第一个参数**
应该是类型为 `suspend () -> T` 的 lambda。
:::

### 步骤 2：配置注解

在您的转换器中配置注解属性：

```kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
                // 您的注解类信息
                classInfo {
                    packageName = "com.example"
                    className = "JBlock"
                }

                // 您的注解中的属性名称
                baseNameProperty = "baseName"      // 默认是 `baseName`
                suffixProperty = "suffix"          // 默认是 `suffix`
                asPropertyProperty = "asProperty"  // 默认是 `asProperty`

                // 默认值（编译器插件无法获取注解默认值）
                defaultSuffix = "Blocking"
                defaultAsProperty = false
            }
        }
    }
}
```

### 步骤 3：配置转换函数

配置您的自定义转换函数：

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            markAnnotation {
                // ... 注解配置
            }

            // 转换函数信息
            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inBlock"
            }

            // 返回类型配置
            transformReturnType = null  // null 表示与原函数相同的类型
            transformReturnTypeGeneric = false  // 如果返回类型有泛型则为 true
        }
    }
}
```

## 高级配置

### 自定义属性名称

您可以在注解中使用不同的属性名称：

```kotlin
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)
```

配置映射：

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JBlock"
                }

                // 映射到您的自定义属性名称
                baseNameProperty = "myBaseName"
                suffixProperty = "mySuffix"
                asPropertyProperty = "myAsProperty"
                
                defaultSuffix = "Blocking"
                defaultAsProperty = false
            }
        }
    }
}
```

### 返回类型配置

#### 相同返回类型

对于返回与原函数相同类型的函数：

```kotlin
transformReturnType = null
transformReturnTypeGeneric = false
```

#### 泛型返回类型

对于返回包含原类型的泛型类型的函数（例如，`CompletableFuture<T>`）：

```kotlin
transformReturnType = "java.util.concurrent.CompletableFuture"
transformReturnTypeGeneric = true
```

#### 特定返回类型

对于返回没有泛型的特定类型的函数（例如，`Job`）：

```kotlin
transformReturnType = "kotlinx.coroutines.Job"
transformReturnTypeGeneric = false
```

## 注解管理

### 向原函数添加注解

向原挂起函数添加注解：

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            // ... 其他配置

            // 向原函数添加 @JvmSynthetic
            addOriginFunctionIncludeAnnotation {
                classInfo {
                    packageName = "kotlin.jvm"
                    className = "JvmSynthetic"
                }
                repeatable = false  // 默认是 false
            }
        }
    }
}
```

### 向生成的函数添加注解

向生成的合成函数添加注解：

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            // ... 其他配置

            // 向生成的函数添加自定义 @JApi 注解
            addSyntheticFunctionIncludeAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JApi"
                }
                includeProperty = true  // 可以添加到属性
            }
        }
    }
}
```

### 复制注解

启用从原函数到合成函数的注解复制：

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            // ... 其他配置

            // 启用注解复制
            copyAnnotationsToSyntheticFunction = true
            copyAnnotationsToSyntheticProperty = true  // 对于属性

            // 从复制中排除特定注解
            addCopyAnnotationExclude {
                classInfo {
                    packageName = "kotlin.jvm"
                    className = "JvmSynthetic"
                }
            }
        }
    }
}
```

## 完整示例

这是一个自定义转换器的完整示例：

### 自定义组件

```kotlin
// 自定义注解
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)

// 自定义警告注解
@RequiresOptIn(message = "Api for Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class JApi

// 自定义转换函数
fun <T> inBlock(block: suspend () -> T): T {
    // 您的自定义阻塞实现
    TODO("Your impl")
}
```

### 配置 {#complete-example-configuration}

```kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            // 注解配置
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
          
            // 转换函数配置
            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inBlock"
            }
          
            // 注解管理
            copyAnnotationsToSyntheticFunction = true
            copyAnnotationsToSyntheticProperty = true

            // 向原函数添加 @JvmSynthetic
            addOriginFunctionIncludeAnnotation {
                classInfo {
                    from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
                }
                repeatable = false
            }

            // 向生成的函数添加 @JApi
            addSyntheticFunctionIncludeAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JApi"
                }
                includeProperty = true
            }

            // 从复制中排除 @JvmSynthetic
            addCopyAnnotationExclude {
                from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
            }
        }
    }
}
```

## 多用途注解

您可以通过使用不同的属性名称创建与多个转换器一起工作的注解：

### 注解定义

```kotlin
annotation class JTrans(
    val blockingBaseName: String = "",
    val blockingSuffix: String = "Blocking",
    val blockingAsProperty: Boolean = false,
    
    val asyncBaseName: String = "",
    val asyncSuffix: String = "Async",
    val asyncAsProperty: Boolean = false
)
```

### 配置 {#multi-purpose-configuration}

```kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        // 阻塞转换器
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JTrans"
                }
                baseNameProperty = "blockingBaseName"
                suffixProperty = "blockingSuffix"
                asPropertyProperty = "blockingAsProperty"
                defaultSuffix = "Blocking"
                defaultAsProperty = false
            }

            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inBlock"
            }
            // ... 其他配置
        }

        // 异步转换器
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JTrans"
                }
                baseNameProperty = "asyncBaseName"
                suffixProperty = "asyncSuffix"
                asPropertyProperty = "asyncAsProperty"
                defaultSuffix = "Async"
                defaultAsProperty = false
            }

            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inAsync"
            }
            // ... 其他配置
        }
    }
}
```

## 内置配置

插件在
`love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations` 中提供了一些常见配置：

```kotlin
// 常见注解类信息
SuspendTransformConfigurations.jvmSyntheticClassInfo
SuspendTransformConfigurations.kotlinJsExportIgnoreClassInfo

// 在您的配置中使用它们
addCopyAnnotationExclude {
    from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
}
```

## 其他
### MarkName
<VersionBadge version="0.13.0" />

有关自定义注解中 `markName` 的配置，
请参考 [MarkName](../features/mark-name.md#customize)。
