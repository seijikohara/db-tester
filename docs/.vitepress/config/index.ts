import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'
import { shared } from './shared'
import { en } from './en'
import { ja } from './ja'

export default withMermaid(
  defineConfig({
    ...shared,
    locales: {
      root: en,
      ja: ja,
    },
  })
)
