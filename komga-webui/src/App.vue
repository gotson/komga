<template>
  <v-app>
    <router-view/>
  </v-app>
</template>
<script lang="ts">
import Vue from 'vue'

const cookieLocale = 'locale'

export default Vue.extend({
  name: 'App',
  created() {
    if (this.$cookies.isKey(cookieLocale)) {
      const locale = this.$cookies.get(cookieLocale)
      if (this.$i18n.availableLocales.includes(locale)) {
        this.$i18n.locale = locale
      }
    }
  },
})
</script>
<style>
@import "styles/global.css";
</style>
