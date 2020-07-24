import '@mdi/font/css/materialdesignicons.css'
import 'typeface-roboto/index.css'
import Vue from 'vue'
import Vuetify from 'vuetify/lib'
import colors from 'vuetify/lib/util/colors'

import { Touch } from 'vuetify/lib/directives'

Vue.use(Vuetify, {
  directives: {
    Touch,
  },
})

export default new Vuetify({
  icons: {
    iconfont: 'mdi',
  },

  theme: {
    options: {
      customProperties: true,
    },
    themes: {
      light: {
        base: colors.shades.white,
        primary: '#005ed3',
        secondary: '#fec000',
        accent: '#ff0335',
        'contrast-1': colors.grey.lighten4,
      },
      dark: {
        base: colors.shades.black,
        primary: '#78baec',
        secondary: '#fec000',
        accent: '#ff0335',
        'contrast-1': colors.grey.darken4,
      },
    },
  },
})
