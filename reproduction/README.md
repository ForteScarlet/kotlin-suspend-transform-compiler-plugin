# Kotlin Compiler Plugin Issue Reproduction

This project demonstrates an issue with Kotlin compiler plugins that implement `FirDeclarationPredicateRegistrar.registerPredicates()` when used with generic annotations.

## Issue Description

When a Kotlin compiler plugin implements `FirDeclarationPredicateRegistrar.registerPredicates()` and is used to process annotations with generic type parameters, it produces the error:

```
ERROR CLASS: Symbol not found for T
```

This happens because the FIR (Frontend IR) cannot resolve the generic type parameter in the annotation when the `registerPredicates()` method is implemented.

## Project Structure

- **annotation**: A multiplatform module containing a generic annotation `GenericAnnotation<T>`
- **compiler-plugin**: A minimal compiler plugin that implements `FirDeclarationPredicateRegistrar.registerPredicates()`
- **compiler-plugin-embeddable**: An embeddable version of the compiler plugin
- **gradle-plugin**: A Gradle plugin that applies the compiler plugin to projects
- **test-app**: A simple application that uses the generic annotation and is compiled with the compiler plugin

## How to Reproduce

1. Build the embeddable compiler plugin:
   ```
   ./gradlew :compiler-plugin-embeddable:build
   ```

2. Try to compile the test application:
   ```
   ./gradlew :test-app:compileKotlin
   ```

This will produce an error similar to:
```
@R|com/example/annotation/GenericAnnotation<ERROR CLASS: Symbol not found for T>|(...)
```

## Workaround

The issue can be worked around by not implementing `FirDeclarationPredicateRegistrar.registerPredicates()` in the compiler plugin. However, this may have other implications for the functionality of the plugin.

## Maven Publishing

All modules in this project are configured to publish to the local Maven repository. To publish them:

```
./gradlew publishToMavenLocal
```

This will publish:
- The annotation module as a multiplatform library
- The compiler plugin module
- The embeddable compiler plugin module
- The Gradle plugin module

## Using the Gradle Plugin

After publishing to the local Maven repository, you can use the Gradle plugin in another project by adding the following to your `build.gradle.kts`:

```kotlin
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("com.example:gradle-plugin:1.0.0")
    }
}

apply(plugin = "com.example.minimal-compiler-plugin")
```

## Compiler Tests

The compiler-plugin module includes comprehensive tests that demonstrate the issue with generic annotations. There are two types of tests:

### 1. Manual FIR Dump Test

This test manually creates a file with code that uses the generic annotation and dumps the FIR structure:

```
./gradlew :compiler-plugin:test --tests "com.example.plugin.test.FirDumpTest"
```

### 2. Generated Compiler Tests

These tests are generated from test data files and provide a more structured way to test the compiler plugin:

1. First, generate the test classes:
   ```
   ./gradlew :compiler-plugin:generateTest
   ```

2. Then run the generated tests:
   ```
   ./gradlew :compiler-plugin:test --tests "com.example.plugin.test.codegen.*"
   ```

The test data files are located in `compiler-plugin/src/testData/codegen/` and include:

- `genericAnnotationIssue.kt`: Demonstrates the issue with generic annotations when using `FirDeclarationPredicateRegistrar.registerPredicates()`

### Viewing the Generated FIR Files

All tests generate FIR dump files in the build directory that you can examine to see how the compiler plugin processes the generic annotation. Look for files with the `.fir.txt` extension in:

```
compiler-plugin/build/test-results/
```

These files will show the error: `ERROR CLASS: Symbol not found for T` when the generic annotation is processed.
