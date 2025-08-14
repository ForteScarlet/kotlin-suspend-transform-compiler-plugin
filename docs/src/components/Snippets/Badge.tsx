import React, {JSX} from 'react';
import { Tooltip } from 'react-tooltip';
import styles from './style.module.css';

interface BadgeProps {
    id?: string;
    type: 'primary' | 'secondary' | 'warning';
    children: React.ReactNode;
    tooltip?: string;
}

export default function Badge({id, type, children, tooltip}: BadgeProps): JSX.Element {
    const badgeId = id || tooltip ? `badge-${Math.random().toString(36).substring(2, 9)}` : undefined;

    return (
        <>
            <span
                id={badgeId}
                className={`badge badge--${type}`}
                style={{marginRight: '6px', marginBottom: '15px'}}
                data-tooltip-id={badgeId}
                data-tooltip-content={tooltip}
            >
                {children}
            </span>
            {tooltip && (
                <Tooltip id={badgeId} place="bottom"/>
            )}
        </>
    );
}

// style={{
//                         backgroundColor: 'var(--ifm-color-emphasis-600)',
//                         color: 'var(--ifm-color-content-inverse)',
//                         fontSize: '0.875rem',
//                         borderRadius: '4px',
//                         padding: '8px 12px',
//                         maxWidth: '300px',
//                         zIndex: 9999
//                     }}
