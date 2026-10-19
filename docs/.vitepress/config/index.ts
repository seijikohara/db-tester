import { defineConfig } from 'vitepress'
import { shared } from './shared'
import { en } from './en'
import { ja } from './ja'

export default defineConfig({
  ...shared,
  locales: {
    root: en,
    ja: ja,
  },
})
