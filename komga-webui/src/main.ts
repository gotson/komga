import _, {LoDashStatic} from 'lodash'
import Vue from 'vue'
// @ts-ignore
import * as lineClamp from 'vue-line-clamp'
import Vuelidate from 'vuelidate'
// @ts-ignore
import Chartkick from 'vue-chartkick'
// @ts-ignore
import Chart from 'chart.js'
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
import komgaTasks from './plugins/komga-tasks.plugin'
import komgaSyncPoints from './plugins/komga-syncpoints.plugin'
import komgaOauth2 from './plugins/komga-oauth2.plugin'
import komgaLogin from './plugins/komga-login.plugin'
import komgaPageHashes from './plugins/komga-pagehashes.plugin'
import komgaMetrics from './plugins/komga-metrics.plugin'
import komgaHistory from './plugins/komga-history.plugin'
import komgaAnnouncements from './plugins/komga-announcements.plugin'
import komgaReleases from './plugins/komga-releases.plugin'
import komgaSettings from './plugins/komga-settings.plugin'
import komgaFonts from './plugins/komga-fonts.plugin'
import vuetify from './plugins/vuetify'
import logger from './plugins/logger.plugin'
import './public-path'
import router from './router'
import store from './store'
import i18n from './i18n'

Vue.prototype.$_ = _
Vue.prototype.$eventHub = new Vue()

Chartkick.options = {
  colors: [
    '#7eb0d5', '#fd7f6f', '#b2e061', '#ffb55a',
    '#8bd3c7', '#ffee65', '#bd7ebe', '#fdcce5',
    '#beb9db', '#ea5545', '#f46a9b', '#ef9b20',
    '#edbf33', '#ede15b', '#bdcf32', '#87bc45',
    '#27aeef', '#b33dc6',
  ],
}

Vue.use(Vuelidate)
Vue.use(lineClamp)
Vue.use(Chartkick.use(Chart))

Vue.use(httpPlugin)
Vue.use(logger)
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
Vue.use(komgaTasks, {http: Vue.prototype.$http})
Vue.use(komgaSyncPoints, {http: Vue.prototype.$http})
Vue.use(komgaOauth2, {http: Vue.prototype.$http})
Vue.use(komgaLogin, {http: Vue.prototype.$http})
Vue.use(komgaPageHashes, {http: Vue.prototype.$http})
Vue.use(komgaMetrics, {http: Vue.prototype.$http})
Vue.use(komgaHistory, {http: Vue.prototype.$http})
Vue.use(komgaAnnouncements, {http: Vue.prototype.$http})
Vue.use(komgaReleases, {http: Vue.prototype.$http})
Vue.use(komgaSettings, {http: Vue.prototype.$http})
Vue.use(komgaFonts, {http: Vue.prototype.$http})

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
