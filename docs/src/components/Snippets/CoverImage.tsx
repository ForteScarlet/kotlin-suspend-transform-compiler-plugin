import React from 'react';

export default function CoverImage(): JSX.Element {
  return (
    <img src={require('@site/static/img/cover.png').default} alt="cover" />
  );
}
