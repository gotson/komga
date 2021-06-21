import _, {LoDashStatic} from 'lodash'
import Vue from 'vue'
import VueCookies from 'vue-cookies'
// @ts-ignore
import * as lineClamp from 'vue-line-clamp'
import Vuelidate from 'vuelidate'
import {sync} from 'vuex-router-sync'
import App from './App.vue'
import actuator from './plugins/actuator.plugin'
import httpPlugin from './plugins/http.plugin'
import komgaBooks from './plugins/komga-books.plugin'
import komgaClaim from './plugins/komga-claim.plugin'
import komgaCollections from './plugins/komga-collections.plugin'
import komgaReadLists from './plugins/komga-readlists.plugin'
import komgaFileSystem from './plugins/komga-filesystem.plugin'
import komgaLibraries from './plugins/komga-libraries.plugin'
import komgaReferential from './plugins/komga-referential.plugin'
import komgaSeries from './plugins/komga-series.plugin'
import komgaUsers from './plugins/komga-users.plugin'
import komgaTransientBooks from './plugins/komga-transientbooks.plugin'
import komgaSse from './plugins/komga-sse.plugin'
import vuetify from './plugins/vuetify'
import './public-path'
import router from './router'
import store from './store'
import i18n from './i18n'

Vue.prototype.$_ = _
Vue.prototype.$eventHub = new Vue()

Vue.use(Vuelidate)
Vue.use(lineClamp)
Vue.use(VueCookies)

Vue.use(httpPlugin)
Vue.use(komgaFileSystem, {http: Vue.prototype.$http})
Vue.use(komgaSeries, {http: Vue.prototype.$http})
Vue.use(komgaCollections, {http: Vue.prototype.$http})
Vue.use(komgaReadLists, {http: Vue.prototype.$http})
Vue.use(komgaBooks, {http: Vue.prototype.$http})
Vue.use(komgaReferential, {http: Vue.prototype.$http})
Vue.use(komgaClaim, {http: Vue.prototype.$http})
Vue.use(komgaTransientBooks, {http: Vue.prototype.$http})
Vue.use(komgaUsers, {store: store, http: Vue.prototype.$http})
Vue.use(komgaLibraries, {store: store, http: Vue.prototype.$http})
Vue.use(komgaSse, {eventHub: Vue.prototype.$eventHub, store: store})
Vue.use(actuator, {http: Vue.prototype.$http})


Vue.config.productionTip = false

sync(store, router)

new Vue({
  router,
  store,
  vuetify,
  i18n,
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
