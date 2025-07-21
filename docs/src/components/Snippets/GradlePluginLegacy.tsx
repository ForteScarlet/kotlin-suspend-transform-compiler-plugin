import React from 'react';
import CodeBlock from '@theme/CodeBlock';

export default function GradlePluginLegacy(): JSX.Element {
  const code = `buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:%version%")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") // or multiplatform?
    id("love.forte.plugin.suspend-transform")
    // Others ...
}`;

  return (
    <CodeBlock language="kotlin">{code}</CodeBlock>
  );
}
