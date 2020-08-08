import KomgaUsersService from '@/services/komga-users.service'
import { UserRoles } from '@/types/enum-users'
import { AxiosInstance } from 'axios'
import _Vue from 'vue'
import { Module } from 'vuex/types'

let service: KomgaUsersService

const vuexModule: Module<any, any> = {
  state: {
    me: {} as UserDto,
    users: [] as UserWithSharedLibrariesDto[],
  },
  getters: {
    meAdmin: state => state.me.hasOwnProperty('roles') && state.me.roles.includes(UserRoles.ADMIN),
    meFileDownload: state => state.me.hasOwnProperty('roles') && state.me.roles.includes(UserRoles.FILE_DOWNLOAD),
    mePageStreaming: state => state.me.hasOwnProperty('roles') && state.me.roles.includes(UserRoles.PAGE_STREAMING),
    authenticated: state => state.me.hasOwnProperty('id'),
  },
  mutations: {
    setMe (state, user: UserDto) {
      state.me = user
    },
    setAllUsers (state, users: UserWithSharedLibrariesDto[]) {
      state.users = users
    },
  },
  actions: {
    async getMeWithAuth ({ commit }, { login, password }: { login: string, password: string }) {
      commit('setMe', await service.getMeWithAuth(login, password))
    },
    async logout ({ commit }) {
      try {
        await service.logout()
      } catch (e) {
      }
      commit('setMe', {})
    },
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
    async updateUserRoles ({ dispatch }, { userId, roles }: { userId: string, roles: RolesUpdateDto }) {
      await service.patchUserRoles(userId, roles)
      dispatch('getAllUsers')
    },
    async deleteUser ({ dispatch }, user: UserDto) {
      await service.deleteUser(user)
      dispatch('getAllUsers')
    },
    async updateUserSharedLibraries ({ dispatch }, { user, sharedLibraries }: { user: UserDto, sharedLibraries: SharedLibrariesUpdateDto }) {
      await service.patchUserSharedLibraries(user, sharedLibraries)
      dispatch('getAllUsers')
    },
  },
}

export default {
  install (
    Vue: typeof _Vue,
    { store, http }: { store: any, http: AxiosInstance }) {
    service = new KomgaUsersService(http)
    Vue.prototype.$komgaUsers = service

    store.registerModule('komgaUsers', vuexModule)
  },
}

declare module 'vue/types/vue' {
  interface Vue {
    $komgaUsers: KomgaUsersService
  }
}
