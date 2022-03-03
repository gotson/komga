<template>
  <v-app>
    <router-view/>
  </v-app>
</template>
<script lang="ts">
import Vue from 'vue'
import {Theme} from '@/types/themes'
import {LIBRARY_ADDED, LIBRARY_CHANGED, LIBRARY_DELETED, SESSION_EXPIRED} from '@/types/events'
import {LibrarySseDto, SessionExpiredDto} from '@/types/komga-sse'

export default Vue.extend({
  name: 'App',
  created() {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', this.systemThemeChange)

    this.$eventHub.$on(LIBRARY_ADDED, this.reloadLibraries)
    this.$eventHub.$on(LIBRARY_DELETED, this.reloadLibraries)
    this.$eventHub.$on(LIBRARY_CHANGED, this.reloadLibraries)

    this.$eventHub.$on(SESSION_EXPIRED, this.logout)
  },
  beforeDestroy() {
    window.matchMedia('(prefers-color-scheme: dark)').removeEventListener('change', this.systemThemeChange)

    this.$eventHub.$off(LIBRARY_ADDED, this.reloadLibraries)
    this.$eventHub.$off(LIBRARY_DELETED, this.reloadLibraries)
    this.$eventHub.$off(LIBRARY_CHANGED, this.reloadLibraries)

    this.$eventHub.$off(SESSION_EXPIRED, this.logout)
  },
  watch: {
    '$store.state.persistedState.locale': {
      handler(val) {
        if (this.$i18n.availableLocales.includes(val)) {
          this.$i18n.locale = val
          this.$vuetify.rtl = (this.$t('common.locale_rtl') === 'true')
        }
      },
      immediate: true,
    },
    '$store.state.persistedState.theme': {
      handler(val) {
        if (Object.values(Theme).includes(val)) {
          this.changeTheme(val)
        }
      },
      immediate: true,
    },
  },
  methods: {
    systemThemeChange() {
      if (this.$store.state.persistedState.theme === Theme.SYSTEM) {
        this.changeTheme(this.$store.state.persistedState.theme)
      }
    },
    changeTheme(theme: Theme) {
      switch (theme) {
        case Theme.DARK:
          this.$vuetify.theme.dark = true
          break

        case Theme.SYSTEM:
          this.$vuetify.theme.dark = (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches)
          break

        default:
          this.$vuetify.theme.dark = false
          break
      }
    },
    reloadLibraries(event: LibrarySseDto) {
      this.$store.dispatch('getLibraries')
    },
    logout(event: SessionExpiredDto) {
      this.$komgaUsers.logout()
      this.$router.push({name: 'login'})
    },
  },
})
</script>
<style>
@import "styles/global.css";
</style>
