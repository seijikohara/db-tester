import type { DefaultTheme, LocaleSpecificConfig } from 'vitepress'

export const ja: LocaleSpecificConfig<DefaultTheme.Config> = {
  label: '日本語',
  lang: 'ja-JP',
  title: 'DB Tester',
  description: 'JUnit、Spock、Kotest対応のデータベーステストフレームワーク',
  themeConfig: {
    nav: [
      { text: 'ホーム', link: '/ja/' },
      { text: '概要', link: '/ja/overview' },
    ],

    sidebar: [
      {
        text: 'はじめに',
        items: [
          { text: '概要', link: '/ja/overview' },
          { text: 'アーキテクチャ', link: '/ja/architecture' },
        ],
      },
      {
        text: 'API リファレンス',
        items: [
          { text: 'パブリック API', link: '/ja/public-api' },
          { text: '設定', link: '/ja/configuration' },
          { text: 'データフォーマット', link: '/ja/data-formats' },
        ],
      },
      {
        text: '応用',
        items: [
          { text: 'データベース操作', link: '/ja/database-operations' },
          { text: 'テストフレームワーク', link: '/ja/test-frameworks' },
          { text: 'SPI', link: '/ja/spi' },
          { text: 'エラーハンドリング', link: '/ja/error-handling' },
        ],
      },
      {
        text: '付録',
        items: [{ text: 'フレームワーク比較', link: '/ja/comparison' }],
      },
    ],

    editLink: {
      pattern: 'https://github.com/seijikohara/db-tester/edit/main/docs/specs/:path',
      text: 'GitHub で編集',
    },

    outline: {
      label: '目次',
      level: [2, 3],
    },

    docFooter: {
      prev: '前のページ',
      next: '次のページ',
    },

    lastUpdated: {
      text: '最終更新',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'short',
      },
    },

    returnToTopLabel: 'トップに戻る',
    sidebarMenuLabel: 'メニュー',
    darkModeSwitchLabel: 'テーマ',
    lightModeSwitchTitle: 'ライトモードに切り替え',
    darkModeSwitchTitle: 'ダークモードに切り替え',
    langMenuLabel: '言語を変更',
    skipToContentLabel: 'コンテンツへスキップ',

    notFound: {
      title: 'ページが見つかりません',
      quote: 'お探しのページは存在しません。',
      linkLabel: 'ホームへ移動',
      linkText: 'ホームへ戻る',
    },
  },
}
