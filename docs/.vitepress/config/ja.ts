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
          { text: 'はじめに', link: '/ja/getting-started' },
          { text: '概要', link: '/ja/overview' },
          { text: 'アーキテクチャ', link: '/ja/architecture' },
        ],
      },
      {
        text: 'API リファレンス',
        collapsed: false,
        items: [
          { text: 'API 概要', link: '/ja/public-api' },
          { text: 'アノテーション', link: '/ja/annotations' },
          { text: 'データセットインターフェース', link: '/ja/dataset-interfaces' },
          { text: 'プログラマティック API', link: '/ja/assertion-api' },
          { text: '例外', link: '/ja/exceptions' },
        ],
      },
      {
        text: '設定',
        items: [
          { text: '設定', link: '/ja/configuration' },
          { text: 'データフォーマット', link: '/ja/data-formats' },
        ],
      },
      {
        text: 'テストフレームワーク',
        collapsed: false,
        items: [
          { text: '概要', link: '/ja/test-frameworks' },
          { text: 'JUnit', link: '/ja/junit' },
          { text: 'Spock', link: '/ja/spock' },
          { text: 'Kotest', link: '/ja/kotest' },
          { text: 'Spring Boot', link: '/ja/spring-boot' },
          { text: 'ライフサイクル', link: '/ja/lifecycle' },
        ],
      },
      {
        text: '内部構造',
        collapsed: true,
        items: [
          { text: 'データベース操作', link: '/ja/database-operations' },
          { text: 'SPI 概要', link: '/ja/spi' },
          { text: 'SPI プロバイダー', link: '/ja/spi-providers' },
          { text: 'SPI 登録', link: '/ja/spi-registration' },
        ],
      },
      {
        text: 'トラブルシューティング',
        collapsed: true,
        items: [
          { text: 'エラーハンドリング', link: '/ja/error-handling' },
          { text: 'トラブルシューティング', link: '/ja/troubleshooting' },
        ],
      },
      {
        text: '付録',
        collapsed: true,
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
