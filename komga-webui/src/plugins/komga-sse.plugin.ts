import _Vue from 'vue'
import KomgaSseService from '@/services/komga-sse.service'
import {Module} from 'vuex'

const vuexModule: Module<any, any> = {
  state: {
    taskCount: 0,
    taskCountByType: {} as { [key: string]: number },
  },
  mutations: {
    setTaskCount (state, event) {
      state.taskCount = event.count
      state.taskCountByType = event.countByType
    },
  },
}

export default {
  install(
    Vue: typeof _Vue,
    {eventHub, store}: { eventHub: _Vue, store: any },
  ) {
    store.registerModule('komgaSse', vuexModule)

    Vue.prototype.$komgaSse = new KomgaSseService(eventHub, store)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaSse: KomgaSseService;
  }
}
