<template>
  <q-drawer
    v-model="appStore.drawer"
    show-if-above
    bordered
  >
    <AppDrawerMenu />
    <!--    <AppDrawerFooter />-->
  </q-drawer>
</template>

<script setup lang="ts">
import { useAppStore } from 'stores/app'

const appStore = useAppStore()
</script>
