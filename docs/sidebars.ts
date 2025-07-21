import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */
const sidebars: SidebarsConfig = {
  // Custom sidebar based on the original Writerside structure
  documentationSidebar: [
    'overview',
    'installation',
    'getting-started',
    {
      type: 'category',
      label: 'Configuration',
      items: [
        'configuration/index',
        'configuration/default-transformers',
        'configuration/custom-transformers',
      ],
    },
    {
      type: 'category',
      label: 'Features',
      items: [
        'features/mark-name',
      ],
    },
    {
      type: 'category',
      label: 'Possibly Useful',
      items: [
        'possibly-useful/jsexport-integration',
        'possibly-useful/wasmjs-limitations',
        // 'possibly-useful/examples', // Uncomment when this file is created
      ],
    },
  ],
};

export default sidebars;
