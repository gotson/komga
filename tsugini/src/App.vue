<template>
  <router-view />
</template>

<script setup lang="ts">
import { useAppStore } from 'stores/app'
import { useQuasar } from 'quasar'

const appStore = useAppStore()
const $q = useQuasar()

// initialize theme on startup
$q.dark.set(appStore.theme)
</script>

<style lang="scss">
@use 'styles/global';
</style>
