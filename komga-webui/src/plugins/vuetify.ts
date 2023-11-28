import '@mdi/font/css/materialdesignicons.css'
import 'typeface-roboto/index.css'
import Vue from 'vue'
import Vuetify from 'vuetify/lib'
import colors from 'vuetify/lib/util/colors'

import {Touch} from 'vuetify/lib/directives'
import i18n from '@/i18n'
import IconFormatLineSpacingDown from '@/components/icons/IconFormatLineSpacingDown.vue'

Vue.use(Vuetify, {
  directives: {
    Touch,
  },
})

export default new Vuetify({
  icons: {
    iconfont: 'mdi',
    values: {
      formatLineSpacingDown: {
        component: IconFormatLineSpacingDown,
      },
    },
  },

  lang: {
    t: (key, ...params) => i18n.t(key, params).toString(),
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
        'contrast-light-2': colors.grey.darken2,
        'diff': colors.green.lighten4,
      },
      dark: {
        base: colors.shades.black,
        primary: '#78baec',
        secondary: '#fec000',
        accent: '#ff0335',
        'contrast-1': colors.grey.darken4,
        'contrast-light-2': colors.grey.lighten2,
        'diff': colors.green.darken4,
      },
    },
  },
})
