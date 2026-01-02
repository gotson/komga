import { defineConfig, presetIcons } from 'unocss'
import { aliases } from 'vuetify/iconsets/mdi'

// build the safelist from the vuetify icon aliases
const vuetifyIcons = Object.keys(aliases).map(
  (key) => `i-mdi:${aliases[key]!.toString().substring(4)}`,
)

export default defineConfig({
  presets: [presetIcons()],
  safelist: vuetifyIcons,
})
