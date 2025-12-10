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
      { text: 'Overview', link: '/01-OVERVIEW' },
    ],

    sidebar: [
      {
        text: 'Specifications',
        items: [
          { text: 'Overview', link: '/01-OVERVIEW' },
          { text: 'Architecture', link: '/02-ARCHITECTURE' },
          { text: 'Public API', link: '/03-PUBLIC-API' },
          { text: 'Configuration', link: '/04-CONFIGURATION' },
          { text: 'Data Formats', link: '/05-DATA-FORMATS' },
          { text: 'Database Operations', link: '/06-DATABASE-OPERATIONS' },
          { text: 'Test Frameworks', link: '/07-TEST-FRAMEWORKS' },
          { text: 'SPI', link: '/08-SPI' },
          { text: 'Error Handling', link: '/09-ERROR-HANDLING' },
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
