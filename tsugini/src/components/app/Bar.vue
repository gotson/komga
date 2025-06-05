<template>
  <q-header
    :elevated="!$q.dark.isActive"
    :class="$q.dark.isActive ? 'bg-dark' : 'bg-white'"
  >
    <q-toolbar>
      <q-btn
        flat
        dense
        round
        icon="mdi-menu"
        :color="$q.dark.isActive ? 'white' : 'dark'"
        class="q-mr-md"
        aria-label="Menu"
        @click="appStore.drawer = !appStore.drawer"
      />

      <RouterLink to="/">
        <q-avatar>
          <img src="~assets/logo.svg" />
        </q-avatar>
      </RouterLink>

      <q-toolbar-title :class="$q.dark.isActive ? 'text-white' : 'text-dark'"
        >Komga</q-toolbar-title
      >

      <ThemeSelector />
    </q-toolbar>
  </q-header>
</template>
<script setup lang="ts">
import { useAppStore } from 'stores/app'

const appStore = useAppStore()
</script>

<script lang="ts"></script>

<style scoped></style>
