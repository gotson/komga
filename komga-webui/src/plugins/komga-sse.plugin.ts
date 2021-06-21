import _Vue from 'vue'
import KomgaSseService from "@/services/komga-sse.service"
import {Module} from "vuex";

const vuexModule: Module<any, any> = {
  state: {
    taskCount: 0,
  },
  mutations: {
    setTaskCount (state, val) {
      state.taskCount = val
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
