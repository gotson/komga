import Vue from 'vue'
import Router from 'vue-router'
import store from './store'

Vue.use(Router)

const lStore = store

const adminGuard = (to: any, from: any, next: any) => {
  if (!lStore.getters.meAdmin) next({ name: 'home' })
  else next()
}

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: { name: 'welcome' },
      component: () => import(/* webpackChunkName: "home" */ './views/Home.vue'),
      children: [
        {
          path: '/libraries/add',
          name: 'addlibrary',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "addlibrary" */ './components/LibraryAddDialog.vue')
        },
        {
          path: '/welcome',
          name: 'welcome',
          component: () => import(/* webpackChunkName: "welcome" */ './components/Welcome.vue')
        },
        {
          path: '/settings',
          name: 'settings',
          redirect: { name: 'settings-users' }
        },
        {
          path: '/settings/users',
          name: 'settings-users',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "settings-users" */ './components/SettingsUsers.vue'),
          children: [
            {
              path: '/settings/users/add',
              name: 'settings-users-add',
              component: () => import(/* webpackChunkName: "settings-user" */ './components/UserAddDialog.vue')
            }
          ]
        },
        {
          path: '/account',
          name: 'account',
          component: () => import(/* webpackChunkName: "account" */ './components/AccountSettings.vue')
        }
      ]
    },
    {
      path: '*',
      name:
        'notfound',
      component:
        () => import(/* webpackChunkName: "notfound" */ './views/PageNotFound.vue')
    }
  ]
})
