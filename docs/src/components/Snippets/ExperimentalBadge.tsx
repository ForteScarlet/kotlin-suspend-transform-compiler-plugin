import React, {JSX} from 'react';
import Translate, {translate} from '@docusaurus/Translate';
import Badge from './Badge';

export default function ExperimentalBadge(): JSX.Element {
    const tooltipText = translate({id: 'badge.experimental.tooltip'});

    return (
        <Badge type="warning" tooltip={tooltipText}>
            <Translate id="badge.experimental"/>
        </Badge>
    );
}