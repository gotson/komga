import { create } from 'storybook/theming'
import komgaSvg from '../src/assets/komga.svg' // Webpack/Vite processes this

export default create({
  base: 'light',
  brandTitle: 'Komga Storybook',
  brandUrl: 'https://komga.org',
  brandImage: komgaSvg,
})
