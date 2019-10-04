import Vue from 'vue'
import Vuelidate from 'vuelidate'
import App from './App.vue'
import httpPlugin from './plugins/http.plugin'
import komgaFileSystem from './plugins/komga-filesystem.plugin'
import komgaLibraries from './plugins/komga-libraries.plugin'
import vuetify from './plugins/vuetify'
import router from './router'
import store from './store'

Vue.use(Vuelidate)

Vue.use(httpPlugin)
Vue.use(komgaFileSystem, { http: Vue.prototype.$http })
Vue.use(komgaLibraries, { store: store, http: Vue.prototype.$http })

Vue.config.productionTip = false

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
