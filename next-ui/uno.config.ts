import { defineConfig, presetIcons } from 'unocss'

export default defineConfig({
  presets: [
    presetIcons({
      // ensures proper CSS layers for icon colors
      processor(props) {
        delete props.color
      },
    }),
  ],
})
