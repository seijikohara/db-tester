import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

export default withMermaid({
  title: 'DB Tester',
  description: 'Database testing framework for JUnit and Spock',
  base: '/db-tester/',

  srcDir: 'specs',
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

  locales: {
    root: {
      label: 'English',
      lang: 'en-US',
      themeConfig: {
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
        editLink: {
          pattern: 'https://github.com/seijikohara/db-tester/edit/main/docs/specs/:path',
          text: 'Edit this page on GitHub',
        },
      },
    },
    ja: {
      label: '日本語',
      lang: 'ja-JP',
      themeConfig: {
        nav: [
          { text: 'ホーム', link: '/ja/' },
          { text: '概要', link: '/ja/01-overview' },
        ],
        sidebar: [
          {
            text: '仕様',
            items: [
              { text: '概要', link: '/ja/01-overview' },
              { text: 'アーキテクチャ', link: '/ja/02-architecture' },
              { text: 'パブリックAPI', link: '/ja/03-public-api' },
              { text: '設定', link: '/ja/04-configuration' },
              { text: 'データフォーマット', link: '/ja/05-data-formats' },
              { text: 'データベース操作', link: '/ja/06-database-operations' },
              { text: 'テストフレームワーク', link: '/ja/07-test-frameworks' },
              { text: 'SPI', link: '/ja/08-spi' },
              { text: 'エラーハンドリング', link: '/ja/09-error-handling' },
            ],
          },
        ],
        outline: { label: '目次', level: [2, 3] },
        docFooter: {
          prev: '前のページ',
          next: '次のページ',
        },
        lastUpdated: { text: '最終更新' },
        editLink: {
          pattern: 'https://github.com/seijikohara/db-tester/edit/main/docs/specs/:path',
          text: 'GitHubで編集',
        },
        returnToTopLabel: 'トップに戻る',
        sidebarMenuLabel: 'メニュー',
        darkModeSwitchLabel: 'テーマ',
        lightModeSwitchTitle: 'ライトモードに切り替え',
        darkModeSwitchTitle: 'ダークモードに切り替え',
        langMenuLabel: '言語',
        skipToContentLabel: 'コンテンツへスキップ',
      },
    },
  },

  themeConfig: {
    logo: '/favicon.svg',
    externalLinkIcon: true,

    socialLinks: [
      { icon: 'github', link: 'https://github.com/seijikohara/db-tester' },
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2025 Seiji Kohara',
    },

    search: {
      provider: 'local',
      options: {
        locales: {
          ja: {
            translations: {
              button: {
                buttonText: '検索',
                buttonAriaLabel: '検索',
              },
              modal: {
                displayDetails: '詳細を表示',
                resetButtonTitle: 'リセット',
                backButtonTitle: '戻る',
                noResultsText: '検索結果が見つかりません',
                footer: {
                  selectText: '選択',
                  navigateText: '移動',
                  closeText: '閉じる',
                },
              },
            },
          },
        },
      },
    },
  },

  vite: {
    publicDir: '../public',
  },

  mermaid: {
    theme: 'default',
  },
})
