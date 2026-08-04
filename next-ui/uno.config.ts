import { defineConfig, presetIcons } from 'unocss'

export default defineConfig({
  content: {
    pipeline: {
      include: [
        // Retain the default file types
        /\.(vue|svelte|[jt]sx|mdx?|astro|elm|php|phtml|html)($|\?)/,
        // Add a glob pattern to include your js/ts files
        'src/**/*.{js,ts}',
      ],
      // You can also explicitly exclude files if needed
      // exclude: ['src/ignored-folder/**/*']
    },
  },
  presets: [
    presetIcons({
      // ensures proper CSS layers for icon colors
      processor(props) {
        delete props.color
      },
    }),
  ],
})
