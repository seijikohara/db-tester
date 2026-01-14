import type { DefaultTheme, LocaleSpecificConfig } from 'vitepress'

export const en: LocaleSpecificConfig<DefaultTheme.Config> = {
  label: 'English',
  lang: 'en-US',
  title: 'DB Tester',
  description: 'Database testing framework for JUnit, Spock and Kotest',
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Overview', link: '/overview' },
    ],

    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Overview', link: '/overview' },
          { text: 'Architecture', link: '/architecture' },
        ],
      },
      {
        text: 'API Reference',
        items: [
          { text: 'Public API', link: '/public-api' },
          { text: 'Configuration', link: '/configuration' },
          { text: 'Data Formats', link: '/data-formats' },
        ],
      },
      {
        text: 'Advanced',
        items: [
          { text: 'Database Operations', link: '/database-operations' },
          { text: 'Test Frameworks', link: '/test-frameworks' },
          { text: 'SPI', link: '/spi' },
          { text: 'Error Handling', link: '/error-handling' },
        ],
      },
      {
        text: 'Appendix',
        items: [{ text: 'Framework Comparison', link: '/comparison' }],
      },
    ],

    editLink: {
      pattern: 'https://github.com/seijikohara/db-tester/edit/main/docs/specs/:path',
      text: 'Edit this page on GitHub',
    },

    outline: {
      label: 'On this page',
      level: [2, 3],
    },

    docFooter: {
      prev: 'Previous page',
      next: 'Next page',
    },

    lastUpdated: {
      text: 'Last updated',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'short',
      },
    },

    returnToTopLabel: 'Return to top',
    sidebarMenuLabel: 'Menu',
    darkModeSwitchLabel: 'Appearance',
    lightModeSwitchTitle: 'Switch to light theme',
    darkModeSwitchTitle: 'Switch to dark theme',
    langMenuLabel: 'Change language',
    skipToContentLabel: 'Skip to content',

    notFound: {
      title: 'Page Not Found',
      quote: 'The page you are looking for does not exist.',
      linkLabel: 'Go to Home',
      linkText: 'Take me home',
    },
  },
}
