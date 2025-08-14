import React, {JSX} from 'react';
import CodeBlock from '@theme/CodeBlock';
import versionInfo from '../../version.json';

export default function GradlePluginDSL(): JSX.Element {
  const code = `plugins {
    kotlin("jvm") version "$KOTLIN_VERSION" // or multiplatform
    id("love.forte.plugin.suspend-transform") version "${versionInfo.version}"
    // Others ...
}`;

  return (
    <CodeBlock language="kotlin">{code}</CodeBlock>
  );
}
