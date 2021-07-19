<template>
  <v-app>
    <router-view/>
  </v-app>
</template>
<script lang="ts">
import Vue from 'vue'
import {Theme} from '@/types/themes'
import {LIBRARY_ADDED, LIBRARY_CHANGED, LIBRARY_DELETED} from '@/types/events'
import {LibrarySseDto} from '@/types/komga-sse'

const cookieLocale = 'locale'
const cookieTheme = 'theme'
const cookieFit = 'webreader.fit'
const cookieContinuousReaderFit = 'webreader.continuousReaderFit'
const cookieContinuousReaderPadding = 'webreader.continuousReaderPadding'
const cookieReadingDirection = 'webreader.readingDirection'
const cookiePageLayout = 'webreader.pageLayout'
const cookieSwipe = 'webreader.swipe'
const cookieAnimations = 'webreader.animations'
const cookieBackground = 'webreader.background'
const cookiePageSize = 'pagesize'

export default Vue.extend({
  name: 'App',
  created() {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', this.systemThemeChange)

    // TODO: remove this after a few months (moved to local storage in 0.85.0 - 29 Mar 2021)
    // remove also vue-cookie npm package
    if (this.$cookies.isKey(cookieLocale)) {
      this.$store.commit('setLocale', this.$cookies.get(cookieLocale))
      this.$cookies.remove(cookieLocale)
    }
    if (this.$cookies.isKey(cookieTheme)) {
      this.$store.commit('setTheme', this.$cookies.get(cookieTheme))
      this.$cookies.remove(cookieTheme)
    }
    if (this.$cookies.isKey(cookieFit)) {
      this.$store.commit('setWebreaderPagedScale', this.$cookies.get(cookieFit))
      this.$cookies.remove(cookieFit)
    }
    if (this.$cookies.isKey(cookieContinuousReaderFit)) {
      this.$store.commit('setWebreaderContinuousScale', this.$cookies.get(cookieContinuousReaderFit))
      this.$cookies.remove(cookieContinuousReaderFit)
    }
    if (this.$cookies.isKey(cookieContinuousReaderPadding)) {
      this.$store.commit('setWebreaderContinuousPadding', parseInt(this.$cookies.get(cookieContinuousReaderPadding)))
      this.$cookies.remove(cookieContinuousReaderPadding)
    }
    if (this.$cookies.isKey(cookieReadingDirection)) {
      this.$store.commit('setWebreaderReadingDirection', this.$cookies.get(cookieReadingDirection))
      this.$cookies.remove(cookieReadingDirection)
    }
    if (this.$cookies.isKey(cookiePageLayout)) {
      this.$store.commit('setWebreaderPagedPageLayout', this.$cookies.get(cookiePageLayout))
      this.$cookies.remove(cookiePageLayout)
    }
    if (this.$cookies.isKey(cookieSwipe)) {
      this.$store.commit('setWebreaderSwipe', this.$cookies.get(cookieSwipe))
      this.$cookies.remove(cookieSwipe)
    }
    if (this.$cookies.isKey(cookieAnimations)) {
      this.$store.commit('setWebreaderAnimations', this.$cookies.get(cookieAnimations))
      this.$cookies.remove(cookieAnimations)
    }
    if (this.$cookies.isKey(cookieBackground)) {
      this.$store.commit('setWebreaderBackground', this.$cookies.get(cookieBackground))
      this.$cookies.remove(cookieBackground)
    }
    if (this.$cookies.isKey(cookiePageSize)) {
      this.$store.commit('setBrowsingPageSize', parseInt(this.$cookies.get(cookiePageSize)))
      this.$cookies.remove(cookiePageSize)
    }
    this.$cookies.keys()
      .filter(x => x.startsWith('collection.filter') || x.startsWith('library.filter') || x.startsWith('library.sort'))
      .forEach(x => this.$cookies.remove(x))


    this.$eventHub.$on(LIBRARY_ADDED, this.reloadLibraries)
    this.$eventHub.$on(LIBRARY_DELETED, this.reloadLibraries)
    this.$eventHub.$on(LIBRARY_CHANGED, this.reloadLibraries)
  },
  beforeDestroy() {
    window.matchMedia('(prefers-color-scheme: dark)').removeEventListener('change', this.systemThemeChange)

    this.$eventHub.$off(LIBRARY_ADDED, this.reloadLibraries)
    this.$eventHub.$off(LIBRARY_DELETED, this.reloadLibraries)
    this.$eventHub.$off(LIBRARY_CHANGED, this.reloadLibraries)
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
  },
})
</script>
<style>
@import "styles/global.css";
</style>
