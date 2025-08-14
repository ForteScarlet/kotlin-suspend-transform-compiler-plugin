import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import versionInfo from './src/version.json';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)
/**
 * @see https://github.com/facebook/docusaurus/issues/4542#issuecomment-1434839071
 */
function getSiteTagline() {
  switch(process.env.DOCUSAURUS_CURRENT_LOCALE) {
    case "zh-CN": return '让 suspend 不再害羞';
    default: return 'Make suspend no longer shy';
  }
}

const config: Config = {
  title: 'Kotlin Suspend Transform Compiler Plugin',
  tagline: getSiteTagline(), // 'Make suspend less shy',
  favicon: 'img/favicon.ico',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Set the production url of your site here
  url: 'https://fortescarlet.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/kotlin-suspend-transform-compiler-plugin/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'ForteScarlet', // Usually your GitHub org/user name.
  projectName: 'kotlin-suspend-transform-compiler-plugin', // Usually your repo name.
  trailingSlash: false,

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en', 'zh-CN'],
    localeConfigs: {
      en: {
        htmlLang: 'en',
      },
      'zh-CN': {
        htmlLang: 'zh-CN',
      },
    },
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          // editUrl: ({locale, docPath}) => {
          //   if (locale === 'zh-CN') {
          //     return `https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/tree/dev/docs/i18n/zh-CN/docusaurus-plugin-content-docs/current/${docPath}`;
          //   }
          //   return `https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/tree/dev/docs/${docPath}`;
          // },
          editUrl: 'https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin/tree/dev/docs/',
          editLocalizedFiles: true,
          showLastUpdateTime: true,
          lastVersion: 'current',
          versions: {
            current: {
              badge: true,
              label: 'v' + versionInfo.version,
            },
          }
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    // image: 'img/docusaurus-social-card.jpg',
    navbar: {
      title: 'Kotlin Suspend Transform Compiler Plugin',
      // logo: {
      //   alt: 'Logo',
      //   src: 'img/logo.svg',
      // },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'documentationSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          type: 'docsVersionDropdown',
          position: 'right',
          versions: ['current'],
        },
        {
          href: 'https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin',
          position: 'right',
          className: 'header-github-link',
          'aria-label': 'GitHub repository',
        },
        {
          type: 'localeDropdown',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Documentation',
              to: '/docs/',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin',
            },
          ],
        },
      ],
      copyright: `Copyright © 2022-${new Date().getFullYear()} ForteScarlet, Inc. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'kotlin'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
