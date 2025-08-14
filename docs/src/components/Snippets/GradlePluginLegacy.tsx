import React, {JSX} from 'react';
import CodeBlock from '@theme/CodeBlock';
import versionInfo from '../../version.json';

export default function GradlePluginLegacy(): JSX.Element {
  const code = `buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:${versionInfo.version}")
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
