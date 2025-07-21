import React from 'react';
import CodeBlock from '@theme/CodeBlock';

export default function GradlePluginDSL(): JSX.Element {
  const code = `plugins {
    kotlin("jvm") version "$KOTLIN_VERSION" // or multiplatform
    id("love.forte.plugin.suspend-transform") version "%version%"
    // Others ...
}`;

  return (
    <CodeBlock language="kotlin">{code}</CodeBlock>
  );
}
