<template>
  <v-app>
    <router-view/>
  </v-app>
</template>
<script lang="ts">
import Vue from 'vue'
import {Theme} from "@/types/themes";

const cookieLocale = 'locale'
const cookieTheme = 'theme'

export default Vue.extend({
  name: 'App',
  created() {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', this.systemThemeChange)

    // TODO: remove this after a few months
    if (this.$cookies.isKey(cookieLocale)) {
      this.$store.commit('setLocale', this.$cookies.get(cookieLocale))
      this.$cookies.remove(cookieLocale)
    }
    if (this.$cookies.isKey(cookieTheme)) {
      this.$store.commit('setTheme', this.$cookies.get(cookieTheme))
      this.$cookies.remove(cookieTheme)
    }
  },
  beforeDestroy() {
    window.matchMedia('(prefers-color-scheme: dark)').removeEventListener('change', this.systemThemeChange)
  },
  watch: {
    "$store.state.persistedState.locale": {
      handler(val) {
        if (this.$i18n.availableLocales.includes(val)) {
          this.$i18n.locale = val
          this.$vuetify.rtl = (this.$t('common.locale_rtl') === 'true')
        }
      },
      immediate: true,
    },
    "$store.state.persistedState.theme": {
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
  },
})
</script>
<style>
@import "styles/global.css";
</style>
