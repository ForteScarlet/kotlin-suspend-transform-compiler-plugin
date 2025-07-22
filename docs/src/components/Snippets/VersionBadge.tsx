import React, {JSX} from 'react';
import Translate, {translate} from '@docusaurus/Translate';
import Badge from './Badge';

interface VersionBadgeProps {
  version: string;
}

export default function ({ version }: VersionBadgeProps): JSX.Element {
  const tooltipText = translate({id: 'badge.version.tooltip'}, { version });

  return (
    <Badge type="secondary" tooltip={tooltipText}>
      <Translate id="badge.version" values={{version}} />
    </Badge>
  );
}