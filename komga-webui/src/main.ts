import _, { LoDashStatic } from 'lodash'
import Vue from 'vue'
// @ts-ignore
import * as lineClamp from 'vue-line-clamp'
import Vuelidate from 'vuelidate'
import { sync } from 'vuex-router-sync'
import App from './App.vue'
import httpPlugin from './plugins/http.plugin'
import komgaFileSystem from './plugins/komga-filesystem.plugin'
import komgaLibraries from './plugins/komga-libraries.plugin'
import komgaSeries from './plugins/komga-series.plugin'
import komgaUsers from './plugins/komga-users.plugin'
import vuetify from './plugins/vuetify'
import router from './router'
import store from './store'

Vue.use(Vuelidate)
Vue.use(lineClamp)

Vue.use(httpPlugin)
Vue.use(komgaFileSystem, { http: Vue.prototype.$http })
Vue.use(komgaSeries, { http: Vue.prototype.$http })
Vue.use(komgaUsers, { store: store, http: Vue.prototype.$http })
Vue.use(komgaLibraries, { store: store, http: Vue.prototype.$http })

Vue.prototype.$_ = _

Vue.config.productionTip = false

sync(store, router)

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')

declare module 'vue/types/vue' {
  interface Vue {
    $_: LoDashStatic;
  }
}
