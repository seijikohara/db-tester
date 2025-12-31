/**
 * Language detection and redirection utility.
 *
 * Supports priority-based matching:
 * 1. Exact match (e.g., 'ja-JP' -> 'ja')
 * 2. Prefix match (e.g., 'ja' -> 'ja')
 * 3. Fallback to default language
 */

export interface LocaleConfig {
  /** Locale path (e.g., 'ja' for /ja/, '' for root) */
  path: string
  /** Language codes that match this locale (e.g., ['ja', 'ja-JP']) */
  matches: string[]
}

export interface LanguageDetectorConfig {
  /** Base path of the site (e.g., '/db-tester/') */
  basePath: string
  /** Supported locales configuration */
  locales: LocaleConfig[]
  /** Default locale path when no match found (e.g., '' for root/English) */
  defaultLocale: string
  /** Storage key for redirect flag */
  storageKey?: string
}

/**
 * Detects browser language and returns the best matching locale path.
 */
export function detectLocale(
  browserLanguages: readonly string[],
  locales: LocaleConfig[],
  defaultLocale: string
): string {
  for (const browserLang of browserLanguages) {
    const normalizedBrowserLang = browserLang.toLowerCase()

    // Exact match
    for (const locale of locales) {
      if (locale.matches.some((m) => m.toLowerCase() === normalizedBrowserLang)) {
        return locale.path
      }
    }

    // Prefix match (e.g., 'ja-JP' matches 'ja')
    const prefix = normalizedBrowserLang.split('-')[0]
    for (const locale of locales) {
      if (locale.matches.some((m) => m.toLowerCase() === prefix)) {
        return locale.path
      }
    }
  }

  return defaultLocale
}

/**
 * Gets browser languages in preference order.
 */
export function getBrowserLanguages(): string[] {
  if (typeof navigator === 'undefined') {
    return []
  }

  const languages: string[] = []

  if (navigator.languages && navigator.languages.length > 0) {
    languages.push(...navigator.languages)
  } else if (navigator.language) {
    languages.push(navigator.language)
  }

  // Fallback for older browsers
  const nav = navigator as Navigator & { userLanguage?: string; browserLanguage?: string }
  if (nav.userLanguage) {
    languages.push(nav.userLanguage)
  }
  if (nav.browserLanguage) {
    languages.push(nav.browserLanguage)
  }

  return languages
}

/**
 * Builds the target URL for the detected locale.
 */
export function buildLocaleUrl(basePath: string, localePath: string): string {
  const base = basePath.endsWith('/') ? basePath : `${basePath}/`
  if (localePath === '') {
    return base
  }
  return `${base}${localePath}/`
}

/**
 * Checks if current path is the root path (where redirection should occur).
 */
export function isRootPath(currentPath: string, basePath: string): boolean {
  const normalizedBase = basePath.endsWith('/') ? basePath : `${basePath}/`
  const normalizedCurrent = currentPath.endsWith('/') ? currentPath : `${currentPath}/`
  return normalizedCurrent === normalizedBase
}

/**
 * Creates a language detector with the given configuration.
 */
export function createLanguageDetector(config: LanguageDetectorConfig) {
  const storageKey = config.storageKey || 'vitepress-lang-redirected'

  return {
    /**
     * Performs language detection and redirection if needed.
     * Returns the target URL if redirection is needed, null otherwise.
     */
    detectAndRedirect(currentPath: string): string | null {
      if (typeof window === 'undefined') {
        return null
      }

      // Only redirect from root path
      if (!isRootPath(currentPath, config.basePath)) {
        return null
      }

      // Check if already redirected in this session
      if (sessionStorage.getItem(storageKey)) {
        return null
      }

      const browserLanguages = getBrowserLanguages()
      const detectedLocale = detectLocale(browserLanguages, config.locales, config.defaultLocale)

      // Mark as redirected
      sessionStorage.setItem(storageKey, 'true')

      // Only redirect if detected locale differs from default (root)
      if (detectedLocale !== config.defaultLocale) {
        return buildLocaleUrl(config.basePath, detectedLocale)
      }

      return null
    },
  }
}
