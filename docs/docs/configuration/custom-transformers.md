---
id: custom-transformers
title: Custom Transformers
---

This guide covers how to create custom transformers when the default transformers don't meet your specific needs.

## Overview

You can create fully customized transformers if the default ones don't meet your requirements. 
For example, if you want to implement blocking logic without using `kotlinx.coroutines.runBlocking`, or if you need platform-specific behavior.

> Reference Implementation: A fully customized implementation of JVM Blocking/Async Transformers can be found at: 
> [simbot's BlockingRunner](https://github.com/simple-robot/simpler-robot/blob/v4-main/simbot-commons/simbot-common-suspend-runner/src/jvmMain/kotlin/love/forte/simbot/suspendrunner/BlockingRunner.kt)

:::warning Known Issue
Custom transformer functions cannot be placed in the same module as the one using the compiler plugin. They need to
be created in a separate module.

For more information: [#100](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/issues/100)
:::

## Basic Setup

When using custom transformers, you typically won't need the default annotation and runtime dependencies:

```kotlin
suspendTransformPlugin {
    // If customized, then you may not use the annotation and runtime we provide.
    includeAnnotation = false
    includeRuntime = false
    
    transformers {
        // Your custom transformer configuration
    }
}
```

## Creating a Custom Transformer

Let's create a custom transformer step by step. We'll create a `@JBlock` annotation that uses a custom `inBlock` function.

### Step 1: Define Your Custom Components

First, define your custom annotation and transform function:

```kotlin
// Your custom annotation
annotation class JBlock(
    val baseName: String = "",
    val suffix: String = "Blocking",
    val asProperty: Boolean = false
)

// Your custom transform function
fun <T> inBlock(block: suspend () -> T): T {
    // Your custom implementation
    TODO("Your impl")
}
```

:::tip Agreement
According to the agreement: **the first parameter** of a transform function 
should be a lambda of type `suspend () -> T`.
:::

### Step 2: Configure the Annotation

Configure the annotation properties in your transformer:

```kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            markAnnotation {
                // Your annotation class information
                classInfo {
                    packageName = "com.example"
                    className = "JBlock"
                }

                // Property names in your annotation
                baseNameProperty = "baseName"      // Default is `baseName`
                suffixProperty = "suffix"          // Default is `suffix`
                asPropertyProperty = "asProperty"  // Default is `asProperty`

                // Default values (compiler plugin can't get annotation defaults)
                defaultSuffix = "Blocking" 
                defaultAsProperty = false
            }
        }
    }
}
```

### Step 3: Configure the Transform Function

Configure your custom transform function:

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            markAnnotation {
                // ... annotation configuration
            }

            // Transform function information
            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inBlock"
            }

            // Return type configuration
            transformReturnType = null  // null means same type as original function
            transformReturnTypeGeneric = false  // true if return type has generics
        }
    }
}
```

## Advanced Configuration

### Custom Property Names

You can use different property names in your annotation:

```kotlin
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)
```

Configure the mapping:

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            markAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JBlock"
                }

                // Map to your custom property names
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

### Return Type Configuration

#### Same Return Type

For functions that return the same type as the original:

```kotlin
transformReturnType = null
transformReturnTypeGeneric = false
```

#### Generic Return Type

For functions that return a generic type containing the original type (e.g., `CompletableFuture<T>`):

```kotlin
transformReturnType = "java.util.concurrent.CompletableFuture"
transformReturnTypeGeneric = true
```

#### Specific Return Type

For functions that return a specific type without generics (e.g., `Job`):

```kotlin
transformReturnType = "kotlinx.coroutines.Job"
transformReturnTypeGeneric = false
```

## Annotation Management

### Adding Annotations to Original Function

Add annotations to the original suspend function:

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            // ... other configuration

            // Add @JvmSynthetic to original function
            addOriginFunctionIncludeAnnotation {
                classInfo {
                    packageName = "kotlin.jvm"
                    className = "JvmSynthetic"
                }
                repeatable = false  // Default is false
            }
        }
    }
}
```

### Adding Annotations to Generated Function

Add annotations to the generated synthetic function:

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            // ... other configuration

            // Add custom @JApi annotation to generated function
            addSyntheticFunctionIncludeAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JApi"
                }
                includeProperty = true  // Can be added to properties
            }
        }
    }
}
```

### Copying Annotations

Enable copying annotations from original to synthetic function:

```kotlin
suspendTransformPlugin {
    transformers {
        addJvm {
            // ... other configuration

            // Enable annotation copying
            copyAnnotationsToSyntheticFunction = true
            copyAnnotationsToSyntheticProperty = true  // For properties

            // Exclude specific annotations from copying
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

## Complete Example

Here's a complete example of a custom transformer:

### Custom Components

```kotlin
// Custom annotation
annotation class JBlock(
    val myBaseName: String = "",
    val mySuffix: String = "Blocking",
    val myAsProperty: Boolean = false
)

// Custom warning annotation
@RequiresOptIn(message = "Api for Java", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
annotation class JApi

// Custom transform function
fun <T> inBlock(block: suspend () -> T): T {
    // Your custom blocking implementation
    TODO("Your impl")
}
```

### Configuration {#complete-example-configuration}

```kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        addJvm {
            // Annotation configuration
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
          
            // Transform function configuration
            transformFunctionInfo {
                packageName = "com.example"
                functionName = "inBlock"
            }
          
            // Annotation management
            copyAnnotationsToSyntheticFunction = true
            copyAnnotationsToSyntheticProperty = true

            // Add @JvmSynthetic to original function
            addOriginFunctionIncludeAnnotation {
                classInfo {
                    from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
                }
                repeatable = false
            }

            // Add @JApi to generated function
            addSyntheticFunctionIncludeAnnotation {
                classInfo {
                    packageName = "com.example"
                    className = "JApi"
                }
                includeProperty = true
            }

            // Exclude @JvmSynthetic from copying
            addCopyAnnotationExclude {
                from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
            }
        }
    }
}
```

## Multi-Purpose Annotations

You can create annotations that work with multiple transformers by using different property names:

### Annotation Definition

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

### Configuration {#multi-purpose-configuration}

```kotlin
suspendTransformPlugin {
    includeAnnotation = false
    includeRuntime = false
    transformers {
        // Blocking transformer
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
            // ... other config
        }

        // Async transformer
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
            // ... other config
        }
    }
}
```

## Built-in Configurations

The plugin provides some common configurations in 
`love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations`:

```kotlin
// Common annotation class infos
SuspendTransformConfigurations.jvmSyntheticClassInfo
SuspendTransformConfigurations.kotlinJsExportIgnoreClassInfo

// Use them in your configuration
addCopyAnnotationExclude {
    from(SuspendTransformConfigurations.jvmSyntheticClassInfo)
}
```

## Others
### MarkName
<span className="badge badge--secondary">Version 0.13.0</span>

For configuration of `markName` in custom annotations, 
refer to [Mark Name](../features/mark-name#customize).
