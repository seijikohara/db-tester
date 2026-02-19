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
          { text: 'Getting Started', link: '/getting-started' },
          { text: 'Overview', link: '/overview' },
          { text: 'Architecture', link: '/architecture' },
        ],
      },
      {
        text: 'API Reference',
        collapsed: false,
        items: [
          { text: 'API Overview', link: '/public-api' },
          { text: 'Annotations', link: '/annotations' },
          { text: 'Dataset Interfaces', link: '/dataset-interfaces' },
          { text: 'Programmatic API', link: '/assertion-api' },
          { text: 'Exceptions', link: '/exceptions' },
        ],
      },
      {
        text: 'Configuration',
        items: [
          { text: 'Configuration', link: '/configuration' },
          { text: 'Data Formats', link: '/data-formats' },
        ],
      },
      {
        text: 'Test Frameworks',
        collapsed: false,
        items: [
          { text: 'Overview', link: '/test-frameworks' },
          { text: 'JUnit', link: '/junit' },
          { text: 'Spock', link: '/spock' },
          { text: 'Kotest', link: '/kotest' },
          { text: 'Spring Boot', link: '/spring-boot' },
          { text: 'Lifecycle', link: '/lifecycle' },
        ],
      },
      {
        text: 'Internals',
        collapsed: true,
        items: [
          { text: 'Database Operations', link: '/database-operations' },
          { text: 'SPI Overview', link: '/spi' },
          { text: 'SPI Providers', link: '/spi-providers' },
          { text: 'SPI Registration', link: '/spi-registration' },
        ],
      },
      {
        text: 'Troubleshooting',
        collapsed: true,
        items: [
          { text: 'Error Handling', link: '/error-handling' },
          { text: 'Troubleshooting', link: '/troubleshooting' },
        ],
      },
      {
        text: 'Appendix',
        collapsed: true,
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
