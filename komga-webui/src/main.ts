import _, { LoDashStatic } from 'lodash'
import Vue from 'vue'
import VueCookies from 'vue-cookies'
// @ts-ignore
import * as lineClamp from 'vue-line-clamp'
import Vuelidate from 'vuelidate'
import { sync } from 'vuex-router-sync'
import App from './App.vue'
import actuator from './plugins/actuator.plugin'
import httpPlugin from './plugins/http.plugin'
import komgaBooks from './plugins/komga-books.plugin'
import komgaClaim from './plugins/komga-claim.plugin'
import komgaCollections from './plugins/komga-collections.plugin'
import komgaFileSystem from './plugins/komga-filesystem.plugin'
import komgaLibraries from './plugins/komga-libraries.plugin'
import komgaReferential from './plugins/komga-referential.plugin'
import komgaSeries from './plugins/komga-series.plugin'
import komgaUsers from './plugins/komga-users.plugin'
import vuetify from './plugins/vuetify'
import './public-path'
import router from './router'
import store from './store'

Vue.use(Vuelidate)
Vue.use(lineClamp)
Vue.use(VueCookies)
Vue.use(require('vue-moment'))

Vue.use(httpPlugin)
Vue.use(komgaFileSystem, { http: Vue.prototype.$http })
Vue.use(komgaSeries, { http: Vue.prototype.$http })
Vue.use(komgaCollections, { http: Vue.prototype.$http })
Vue.use(komgaBooks, { http: Vue.prototype.$http })
Vue.use(komgaReferential, { http: Vue.prototype.$http })
Vue.use(komgaClaim, { http: Vue.prototype.$http })
Vue.use(komgaUsers, { store: store, http: Vue.prototype.$http })
Vue.use(komgaLibraries, { store: store, http: Vue.prototype.$http })
Vue.use(actuator, { http: Vue.prototype.$http })

Vue.prototype.$_ = _
Vue.prototype.$eventHub = new Vue()

Vue.config.productionTip = false

sync(store, router)

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App),
}).$mount('#app')

declare module 'vue/types/vue' {
  interface Vue {
    $_: LoDashStatic;
    $eventHub: Vue;
  }
}

declare global {
  interface Window {
    resourceBaseUrl: string
  }
}
