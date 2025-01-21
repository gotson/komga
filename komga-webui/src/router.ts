import urls from '@/functions/urls'
import Vue from 'vue'
import Router from 'vue-router'
import store from './store'
import {LIBRARIES_ALL, LIBRARY_ROUTE} from '@/types/library'

const qs = require('qs')

Vue.use(Router)

const lStore = store as any

const adminGuard = (to: any, from: any, next: any) => {
  if (!lStore.getters.meAdmin) next({name: 'home'})
  else next()
}

const noLibraryGuard = (to: any, from: any, next: any) => {
  if (lStore.state.komgaLibraries.libraries.length === 0) {
    next({name: 'welcome'})
  } else next()
}

const getLibraryRoute = (libraryId: string) => {
  switch ((lStore.getters.getLibraryRoute(libraryId) as LIBRARY_ROUTE)) {
    case LIBRARY_ROUTE.COLLECTIONS:
      return 'browse-collections'
    case LIBRARY_ROUTE.READLISTS:
      return 'browse-readlists'
    case LIBRARY_ROUTE.BROWSE:
      return 'browse-libraries'
    case LIBRARY_ROUTE.RECOMMENDED:
    default:
      return libraryId === LIBRARIES_ALL ? 'browse-libraries' : 'recommended-libraries'
  }
}

const router = new Router({
  mode: 'history',
  base: urls.base,
  parseQuery(query: string) {
    return qs.parse(query)
  },
  stringifyQuery(query: Object) {
    const res = qs.stringify(query)
    return res ? `?${res}` : ''
  },
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: {name: 'dashboard'},
      component: () => import(/* webpackChunkName: "home" */ './views/HomeView.vue'),
      children: [
        {
          path: '/welcome',
          name: 'welcome',
          component: () => import(/* webpackChunkName: "welcome" */ './views/WelcomeView.vue'),
        },
        {
          path: '/dashboard',
          name: 'dashboard',
          beforeEnter: noLibraryGuard,
          component: () => import(/* webpackChunkName: "dashboard" */ './views/DashboardView.vue'),
        },
        {
          path: '/settings/users',
          name: 'settings-users',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "settings-users" */ './views/SettingsUsers.vue'),
          children: [
            {
              path: '/settings/users/add',
              name: 'settings-users-add',
              component: () => import(/* webpackChunkName: "settings-user" */ './components/dialogs/UserAddDialog.vue'),
            },
          ],
        },
        {
          path: '/settings/server',
          name: 'settings-server',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "settings-server" */ './views/SettingsServer.vue'),
        },
        {
          path: '/settings/metrics',
          name: 'metrics',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "metrics" */ './views/MetricsView.vue'),
        },
        {
          path: '/settings/announcements',
          name: 'announcements',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "announcements" */ './views/AnnouncementsView.vue'),
        },
        {
          path: '/settings/updates',
          name: 'updates',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "updates" */ './views/UpdatesView.vue'),
        },
        {
          path: '/media-management/analysis',
          name: 'media-analysis',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "media-analysis" */ './views/MediaAnalysis.vue'),
        },
        {
          path: '/media-management/missing-posters',
          name: 'missing-posters',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "missing-posters" */ './views/MissingPosters.vue'),
        },
        {
          path: '/media-management/duplicate-files',
          name: 'duplicate-files',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "duplicate-files" */ './views/DuplicateFiles.vue'),
        },
        {
          path: '/media-management/duplicate-pages/known',
          name: 'settings-duplicate-pages-known',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "duplicate-pages-known" */ './views/DuplicatePagesKnown.vue'),
        },
        {
          path: '/media-management/duplicate-pages/unknown',
          name: 'settings-duplicate-pages-unknown',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "duplicate-pages-new" */ './views/DuplicatePagesUnknown.vue'),
        },
        {
          path: '/history',
          name: 'history',
          component: () => import(/* webpackChunkName: "history" */ './views/HistoryView.vue'),
        },
        {
          path: '/account/me',
          name: 'account-me',
          component: () => import(/* webpackChunkName: "account-me" */ './views/AccountView.vue'),
        },
        {
          path: '/account/api-keys',
          name: 'account-api-keys',
          component: () => import(/* webpackChunkName: "account-api-keys" */ './views/ApiKeys.vue'),
        },
        {
          path: '/account/authentication-activity',
          name: 'account-activity',
          component: () => import(/* webpackChunkName: "account-activity" */ './views/SelfAuthenticationActivity.vue'),
        },
        {
          path: '/libraries/:libraryId?',
          name: 'libraries',
          redirect: (route) => ({
            name: getLibraryRoute(route.params.libraryId || LIBRARIES_ALL),
            params: {libraryId: route.params.libraryId || LIBRARIES_ALL},
          }),
        },
        {
          path: '/libraries/:libraryId/recommended',
          name: 'recommended-libraries',
          beforeEnter: noLibraryGuard,
          component: () => import(/* webpackChunkName: "dashboard" */ './views/DashboardView.vue'),
          props: (route) => ({libraryId: route.params.libraryId}),
        },
        {
          path: '/libraries/:libraryId/series',
          name: 'browse-libraries',
          beforeEnter: noLibraryGuard,
          component: () => import(/* webpackChunkName: "browse-libraries" */ './views/BrowseLibraries.vue'),
          props: (route) => ({libraryId: route.params.libraryId}),
        },
        {
          path: '/libraries/:libraryId/collections',
          name: 'browse-collections',
          beforeEnter: noLibraryGuard,
          component: () => import(/* webpackChunkName: "browse-collections" */ './views/BrowseCollections.vue'),
          props: (route) => ({libraryId: route.params.libraryId}),
        },
        {
          path: '/libraries/:libraryId/readlists',
          name: 'browse-readlists',
          beforeEnter: noLibraryGuard,
          component: () => import(/* webpackChunkName: "browse-readlists" */ './views/BrowseReadLists.vue'),
          props: (route) => ({libraryId: route.params.libraryId}),
        },
        {
          path: '/collections/:collectionId',
          name: 'browse-collection',
          component: () => import(/* webpackChunkName: "browse-collection" */ './views/BrowseCollection.vue'),
          props: (route) => ({collectionId: route.params.collectionId}),
        },
        {
          path: '/readlists/:readListId',
          name: 'browse-readlist',
          component: () => import(/* webpackChunkName: "browse-readlist" */ './views/BrowseReadList.vue'),
          props: (route) => ({readListId: route.params.readListId}),
        },
        {
          path: '/series/:seriesId',
          name: 'browse-series',
          component: () => import(/* webpackChunkName: "browse-series" */ './views/BrowseSeries.vue'),
          props: (route) => ({seriesId: route.params.seriesId}),
        },
        {
          path: '/book/:bookId',
          name: 'browse-book',
          component: () => import(/* webpackChunkName: "browse-book" */ './views/BrowseBook.vue'),
          props: (route) => ({bookId: route.params.bookId}),
        },
        {
          path: '/oneshot/:seriesId',
          name: 'browse-oneshot',
          component: () => import(/* webpackChunkName: "browse-oneshot" */ './views/BrowseOneshot.vue'),
          props: (route) => ({seriesId: route.params.seriesId}),
        },
        {
          path: '/search',
          name: 'search',
          component: () => import(/* webpackChunkName: "search" */ './views/SearchView.vue'),
        },
        {
          path: '/import/books',
          name: 'import-books',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "import-books" */ './views/ImportBooks.vue'),
        },
        {
          path: '/import/readlist',
          name: 'import-readlist',
          beforeEnter: adminGuard,
          component: () => import(/* webpackChunkName: "import-readlist" */ './views/ImportReadList.vue'),
        },
      ],
    },
    {
      path: '/startup',
      name: 'startup',
      component: () => import(/* webpackChunkName: "startup" */ './views/StartupView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import(/* webpackChunkName: "login" */ './views/LoginView.vue'),
    },
    {
      path: '/book/:bookId/read',
      name: 'read-book',
      component: () => import(/* webpackChunkName: "read-book" */ './views/DivinaReader.vue'),
      props: (route) => ({bookId: route.params.bookId}),
    },
    {
      path: '/book/:bookId/read-epub',
      name: 'read-epub',
      component: () => import(/* webpackChunkName: "read-epub" */ './views/EpubReader.vue'),
      props: (route) => ({bookId: route.params.bookId}),
    },
    {
      path: '*',
      name: 'notfound',
      component: () => import(/* webpackChunkName: "notfound" */ './views/PageNotFound.vue'),
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      if (to.name !== from.name) {
        return {x: 0, y: 0}
      }
    }
  },
})

router.beforeEach((to, from, next) => {
  // avoid document.title flickering when changing route
  if (!['read-book', 'read-epub', 'browse-book', 'browse-oneshot', 'browse-series', 'browse-libraries',
    'recommended-libraries', 'browse-collection', 'browse-collections', 'browse-readlist', 'browse-readlists'].includes(<string>to.name)
  ) {
    document.title = 'Komga'
  }

  if (window.opener !== null &&
    window.name === 'oauth2Login' &&
    to.query.server_redirect === 'Y'
  ) {
    if (!to.query.error) {
      // authentication succeeded, we redirect the parent window so that it can login via cookie
      window.opener.location.href = urls.origin
    } else {
      // authentication failed, we cascade the error message to the parent
      window.opener.location.href = window.location
    }
    // we can close the popup
    window.close()
  }

  if (to.name !== 'startup' && to.name !== 'login' && !lStore.getters.authenticated) {
    const query = Object.assign({}, to.query, {redirect: to.fullPath})
    next({name: 'startup', query: query})
  } else next()
})

export default router
