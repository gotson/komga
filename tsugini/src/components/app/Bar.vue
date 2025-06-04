<template>
  <q-header elevated>
    <q-toolbar>
      <q-btn
        flat
        dense
        round
        icon="mdi-menu"
        aria-label="Menu"
        @click="appStore.drawer = !appStore.drawer"
      />

      <RouterLink to="/">
        <q-avatar>
          <img src="~assets/logo.svg" />
        </q-avatar>
      </RouterLink>

      <q-toolbar-title>Komga</q-toolbar-title>

      <div>Quasar v{{ $q.version }}</div>
    </q-toolbar>
  </q-header>
</template>
<script setup lang="ts">
import { useAppStore } from 'stores/app'

const appStore = useAppStore()
</script>

<script lang="ts"></script>

<style scoped></style>
