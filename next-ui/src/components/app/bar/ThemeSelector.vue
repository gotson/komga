<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        :icon="themeIcon"
      />
    </template>

    <v-list>
      <v-list-item
        v-for="theme in themes"
        :key="theme.value"
        :prepend-icon="theme.icon"
        :title="theme.title"
        @click="appStore.theme = theme.value"
      />
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import {useAppStore} from '@/stores/app'

const appStore = useAppStore()

const themes= [
  {
    title: 'Light',
    value: 'light',
    icon: 'mdi-brightness-7'
  },
  {
    title: 'Dark',
    value: 'dark',
    icon: 'mdi-brightness-3'
  },
  {
    title: 'System',
    value: 'system',
    icon: 'mdi-brightness-auto'
  },
]

const themeIcon = computed(
  () => themes.find(x => x.value === appStore.theme)?.icon || 'mdi-brightness-auto'
)
</script>

<script lang="ts">
</script>

<style scoped>

</style>
