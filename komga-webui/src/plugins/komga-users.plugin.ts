import KomgaUsersService from '@/services/komga-users.service'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'
import { Module } from 'vuex/types'

let service: KomgaUsersService

const vuexModule: Module<any, any> = {
  state: {
    me: {} as UserDto,
    users: [] as UserDto[]
  },
  getters: {
    meAdmin: state => state.me.hasOwnProperty('roles') && state.me.roles.includes('ADMIN')
  },
  mutations: {
    setMe (state, user: UserDto) {
      state.me = user
    },
    setAllUsers (state, users: UserDto[]) {
      state.users = users
    }
  },
  actions: {
    async getMe ({ commit }) {
      commit('setMe', await service.getMe())
    },
    async updateMyPassword (_, newPassword: PasswordUpdateDto) {
      await service.patchMePassword(newPassword)
    },
    async getAllUsers ({ commit }) {
      commit('setAllUsers', await service.getAll())
    },
    async postUser ({ dispatch }, user: UserCreationDto) {
      await service.postUser(user)
      dispatch('getAllUsers')
    },
    async deleteUser ({ dispatch }, user: UserDto) {
      await service.deleteUser(user)
      dispatch('getAllUsers')
    }
  }
}

export default {
  install (
    Vue: typeof _Vue,
    { store, http }: { store: any, http: AxiosInstance }) {
    service = new KomgaUsersService(http)
    Vue.prototype.$komgaUsers = service

    store.registerModule('komgaUsers', vuexModule)
  }
}

declare module 'vue/types/vue' {
  interface Vue {
    $$komgaUsers: KomgaUsersService;
  }
}
