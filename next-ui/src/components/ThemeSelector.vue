<template>
  <v-btn
    :icon="themeIcon"
    @click="cycleTheme()"
  />
</template>

<script setup lang="ts">
import {useAppStore} from '@/stores/app'

const appStore = useAppStore()

const themes= [
  {
    value: 'light',
    icon: 'mdi-weather-sunny'
  },
  {
    value: 'dark',
    icon: 'mdi-weather-night'
  },
  {
    value: 'system',
    icon: 'mdi-theme-light-dark'
  },
]

const themeIcon = computed(
  () => themes.find(x => x.value === appStore.theme)?.icon || 'mdi-brightness-auto'
)

function cycleTheme() {
  const index = themes.findIndex(x => x.value === appStore.theme)
  const newIndex = (index + 1) % themes.length
  appStore.theme = themes[newIndex]?.value
}
</script>

<script lang="ts">
</script>

<style scoped>

</style>
