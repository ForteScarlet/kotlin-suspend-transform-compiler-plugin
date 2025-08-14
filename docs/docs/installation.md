---
id: installation
title: Installation
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import { GradlePluginDSL, GradlePluginLegacy } from '@site/src/components/Snippets';
import versionInfo from '@site/src/version.json';

This guide covers how to install and set up the Kotlin Suspend Transform Compiler Plugin in your project.

## Version Information

- The latest version on which the current document is based
  <small>(usually a version that does not include `Beta` or `RC`, etc.)</small>:  <br/>
  **v{versionInfo.version}**

- The latest version in the Maven repository and Gradle plugin portal: <br/>
  [![Maven Central](https://img.shields.io/maven-central/v/love.forte.plugin.suspend-transform/suspend-transform-plugin)](https://repo1.maven.org/maven2/love/forte/plugin/suspend-transform/suspend-transform-plugin/)
  [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/love.forte.plugin.suspend-transform)](https://plugins.gradle.org/plugin/love.forte.plugin.suspend-transform)

- The all releases: [Go to GitHub Releases](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/releases)

### Version Naming Convention

Before `0.9.0` (included), the naming convention for versions was `x.y.z`. 
But it seems that the contents of the Kotlin compiler may find changes with each version, 
and such version numbers do not reflect the corresponding Kotlin version, 
and may lead to some confusion as a result.

Therefore, starting after `0.9.0`, versions will be named in the form `$Kotlin-$plugin`, 
e.g. `2.0.20-0.9.1`. 
The first half is the version of Kotlin used for the build, while the second half is the version of this plugin.

### Version Compatibility Table

If the version is less than or equal to `0.9.0`, you can refer to this comparison table:

| Kotlin version | plugin version          |
|----------------|-------------------------|
| `2.0.0`        | `0.8.0-beta1` ~ `0.9.0` |
| `1.9.22`       | `0.7.0-beta1`           |
| `1.9.21`       | `0.6.0`                 |
| `1.9.10`       | `0.5.1`                 |
| `1.9.0`        | `0.5.0`                 |
| `1.8.21`       | `0.3.1` ~ `0.4.0`       |

:::note
I haven't documented in detail the compiler plugin compatibility between each Kotlin version.
From my memory and guess, Kotlin versions have a higher probability of incompatibility when minor is added (e.g. `1.8.0` -> `1.9.0`), 
and a smaller probability of incompatibility when patch is added (e.g. `1.9.21` -> `1.9.22`).
:::

## Gradle Setup

<Tabs>
  <TabItem value="plugin-dsl" label="Plugins DSL">

**build.gradle.kts**

<GradlePluginDSL></GradlePluginDSL>

  </TabItem>
  <TabItem value="legacy-plugin-application" label="Legacy Plugin Application">

**build.gradle.kts**

<GradlePluginLegacy></GradlePluginLegacy>

  </TabItem>
</Tabs>

```kotlin
// config it.
suspendTransformPlugin {
    // Config the SuspendTransformPluginExtension ...
}
```

## Configuration

For more information on this topic, please refer to [Configuration](./configuration/configuration.md).

## Dependencies

Plugins may automatically introduce some dependencies. 
For more information on this topic, please refer to [Configuration - Include Dependencies](./configuration/configuration.md#include-dependencies).

## Cautions
### Gradle JVM

**Gradle JVM** must be JDK11+.

### K2 Support

K2 is supported since `v0.7.0`.

### IDE support in current development projects

The IDE does **NOT** support highlighting for projects that currently use compiler plugins.
If you want to verify that the compiler plugin is working, you can:
  
- JVM platform:
  - Check the compiled `.class` file or its decompiled result.
  - Use reflection to access the expected function in unit testing.
- JS platform:
  - Check whether the function is generated correctly by generating a `.d.ts` file.
  - Use `dynamic` access to the expected function in unit testing.
