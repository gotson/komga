import '@mdi/font/css/materialdesignicons.css'
import 'typeface-roboto/index.css'
import Vue from 'vue'
import Vuetify from 'vuetify/lib'

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
        primary: '#005ed3',
        secondary: '#fec000',
        accent: '#ff0335',
      },
    },
  },
})
