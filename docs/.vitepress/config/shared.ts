import type { DefaultTheme, UserConfig } from 'vitepress'
import { defineConfig } from 'vitepress'

export const shared = defineConfig({
  title: 'DB Tester',
  description: 'Database testing framework for JUnit, Spock and Kotest',
  base: '/db-tester/',

  srcDir: 'specs',
  lastUpdated: true,
  cleanUrls: true,

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/db-tester/favicon.svg' }],
    ['meta', { name: 'theme-color', content: '#5f67ee' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:site_name', content: 'DB Tester' }],
    ['meta', { property: 'og:url', content: 'https://seijikohara.github.io/db-tester/' }],
    ['meta', { property: 'og:image', content: 'https://seijikohara.github.io/db-tester/og-image.png' }],
    ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
  ],

  sitemap: {
    hostname: 'https://seijikohara.github.io/db-tester/',
  },

  markdown: {
    theme: {
      light: 'github-light',
      dark: 'github-dark',
    },
    codeTransformers: [
      {
        postprocess(code) {
          return code.replace(/\[!!code/g, '[!code')
        },
      },
    ],
  },

  themeConfig: {
    logo: '/favicon.svg',
    externalLinkIcon: true,

    socialLinks: [
      { icon: 'github', link: 'https://github.com/seijikohara/db-tester' },
    ],

    search: {
      provider: 'local',
      options: {
        detailedView: true,
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

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2025 Seiji Kohara',
    },
  } satisfies DefaultTheme.Config,

  vite: {
    publicDir: '../public',
  },

  mermaid: {
    theme: 'neutral',
  },
} as UserConfig)
