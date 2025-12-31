import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import { createLanguageDetector } from './languageDetector'
import './custom.css'

/**
 * Locale configuration for the site.
 * Add new languages here to support automatic detection.
 */
const languageDetector = createLanguageDetector({
  basePath: '/db-tester/',
  defaultLocale: '', // English (root)
  locales: [
    {
      path: '', // Root path for English
      matches: ['en', 'en-US', 'en-GB', 'en-AU', 'en-CA'],
    },
    {
      path: 'ja',
      matches: ['ja', 'ja-JP'],
    },
    // Add more locales here:
    // {
    //   path: 'zh',
    //   matches: ['zh', 'zh-CN', 'zh-TW', 'zh-HK'],
    // },
    // {
    //   path: 'ko',
    //   matches: ['ko', 'ko-KR'],
    // },
  ],
})

const theme: Theme = {
  extends: DefaultTheme,
  enhanceApp({ router }) {
    if (typeof window !== 'undefined') {
      const targetUrl = languageDetector.detectAndRedirect(window.location.pathname)
      if (targetUrl) {
        router.go(targetUrl)
      }
    }
  },
}

export default theme
