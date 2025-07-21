import React from 'react';

interface BadgeProps {
  type: 'primary' | 'secondary';
  children: React.ReactNode;
}

export default function Badge({ type, children }: BadgeProps): JSX.Element {
  return (
    <span className={`badge badge--${type}`}>{children}</span>
  );
}
