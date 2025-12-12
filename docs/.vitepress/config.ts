import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

export default withMermaid({
  title: 'DB Tester',
  description: 'Database testing framework for JUnit and Spock',
  base: '/db-tester/',

  srcDir: 'specs',
  lang: 'en-US',
  lastUpdated: true,
  cleanUrls: true,

  markdown: {
    theme: {
      light: 'catppuccin-latte',
      dark: 'catppuccin-mocha',
    },
  },

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/db-tester/favicon.svg' }],
  ],

  themeConfig: {
    logo: '/favicon.svg',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Overview', link: '/01-overview' },
    ],

    sidebar: [
      {
        text: 'Specifications',
        items: [
          { text: 'Overview', link: '/01-overview' },
          { text: 'Architecture', link: '/02-architecture' },
          { text: 'Public API', link: '/03-public-api' },
          { text: 'Configuration', link: '/04-configuration' },
          { text: 'Data Formats', link: '/05-data-formats' },
          { text: 'Database Operations', link: '/06-database-operations' },
          { text: 'Test Frameworks', link: '/07-test-frameworks' },
          { text: 'SPI', link: '/08-spi' },
          { text: 'Error Handling', link: '/09-error-handling' },
        ],
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/seijikohara/db-tester' },
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2025 Seiji Kohara',
    },

    search: {
      provider: 'local',
    },

    editLink: {
      pattern: 'https://github.com/seijikohara/db-tester/edit/main/docs/specs/:path',
      text: 'Edit this page on GitHub',
    },
  },

  vite: {
    publicDir: '../public',
  },

  mermaid: {
    theme: 'default',
  },
})
