import type { DefaultTheme, HeadConfig, UserConfig } from 'vitepress'
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
    ['meta', { name: 'author', content: 'Seiji Kohara' }],
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

  transformHead: ({ pageData }) => {
    const baseUrl = 'https://seijikohara.github.io/db-tester'
    const isJaPage = pageData.relativePath.startsWith('ja/')
    const locale = isJaPage ? 'ja' : 'en'

    // Generate canonical URL
    const canonicalUrl = `${baseUrl}/${pageData.relativePath.replace(/index\.md$/, '').replace(/\.md$/, '')}`

    // Determine if this is a home page (index.md or ja/index.md)
    const isHomePage = pageData.relativePath === 'index.md' || pageData.relativePath === 'ja/index.md'

    // Get title and description from frontmatter or fallback to defaults
    const title = pageData.frontmatter.title || pageData.title || 'DB Tester'
    const description =
      pageData.frontmatter.description ||
      pageData.description ||
      'Database testing framework for JUnit, Spock and Kotest'

    const head: HeadConfig[] = [
      // Canonical URL
      ['link', { rel: 'canonical', href: canonicalUrl }],

      // hreflang links
      ['link', { rel: 'alternate', hreflang: 'en', href: `${baseUrl}/${pageData.relativePath.replace(/^ja\//, '').replace(/index\.md$/, '').replace(/\.md$/, '')}` }],
      ['link', { rel: 'alternate', hreflang: 'ja', href: `${baseUrl}/ja/${pageData.relativePath.replace(/^ja\//, '').replace(/index\.md$/, '').replace(/\.md$/, '')}` }],
      ['link', { rel: 'alternate', hreflang: 'x-default', href: `${baseUrl}/${pageData.relativePath.replace(/^ja\//, '').replace(/index\.md$/, '').replace(/\.md$/, '')}` }],

      // og:locale and og:locale:alternate
      ['meta', { property: 'og:locale', content: locale === 'ja' ? 'ja_JP' : 'en_US' }],
      ['meta', { property: 'og:locale:alternate', content: locale === 'ja' ? 'en_US' : 'ja_JP' }],

      // og:title, og:description, og:url
      ['meta', { property: 'og:title', content: title }],
      ['meta', { property: 'og:description', content: description }],
      ['meta', { property: 'og:url', content: canonicalUrl }],

      // description meta tag
      ['meta', { name: 'description', content: description }],

      // twitter:title, twitter:description
      ['meta', { name: 'twitter:title', content: title }],
      ['meta', { name: 'twitter:description', content: description }],
    ]

    // Add JSON-LD structured data
    if (isHomePage) {
      // Home pages: Organization, WebSite, SoftwareApplication schemas
      const structuredData = {
        '@context': 'https://schema.org',
        '@graph': [
          {
            '@type': 'Organization',
            '@id': `${baseUrl}/#organization`,
            name: 'Seiji Kohara',
            url: baseUrl,
            logo: {
              '@type': 'ImageObject',
              url: `${baseUrl}/og-image.png`,
            },
          },
          {
            '@type': 'WebSite',
            '@id': `${baseUrl}/#website`,
            url: baseUrl,
            name: 'DB Tester',
            description: 'Database testing framework for JUnit, Spock and Kotest',
            publisher: {
              '@id': `${baseUrl}/#organization`,
            },
            inLanguage: locale === 'ja' ? 'ja-JP' : 'en-US',
          },
          {
            '@type': 'SoftwareApplication',
            '@id': `${baseUrl}/#softwareapplication`,
            name: 'DB Tester',
            description: 'Database testing framework for JUnit, Spock and Kotest',
            applicationCategory: 'DeveloperApplication',
            operatingSystem: 'Cross-platform',
            offers: {
              '@type': 'Offer',
              price: '0',
              priceCurrency: 'USD',
            },
            publisher: {
              '@id': `${baseUrl}/#organization`,
            },
          },
        ],
      }

      head.push(['script', { type: 'application/ld+json' }, JSON.stringify(structuredData)])
    } else {
      // Other pages: BreadcrumbList schema
      const pathSegments = pageData.relativePath
        .replace(/^ja\//, '')
        .replace(/index\.md$/, '')
        .replace(/\.md$/, '')
        .split('/')
        .filter(Boolean)

      const breadcrumbList = {
        '@context': 'https://schema.org',
        '@type': 'BreadcrumbList',
        itemListElement: [
          {
            '@type': 'ListItem',
            position: 1,
            name: 'Home',
            item: `${baseUrl}${isJaPage ? '/ja' : ''}/`,
          },
          ...pathSegments.map((segment, index) => ({
            '@type': 'ListItem',
            position: index + 2,
            name: segment.charAt(0).toUpperCase() + segment.slice(1).replace(/-/g, ' '),
            item: `${baseUrl}${isJaPage ? '/ja' : ''}/${pathSegments.slice(0, index + 1).join('/')}/`,
          })),
        ],
      }

      head.push(['script', { type: 'application/ld+json' }, JSON.stringify(breadcrumbList)])
    }

    return head
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
