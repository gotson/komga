<template>
  <q-btn
    :icon="themeIcon"
    round
    flat
    :color="$q.dark.isActive ? 'white' : 'black'"
    @click="cycleTheme()"
  />
</template>

<script setup lang="ts">
import { useAppStore } from 'stores/app'
import { useQuasar } from 'quasar'

const appStore = useAppStore()
const $q = useQuasar()

const themes = [
  {
    value: false,
    icon: 'mdi-weather-sunny',
  },
  {
    value: true,
    icon: 'mdi-weather-night',
  },
  {
    value: 'auto',
    icon: 'mdi-theme-light-dark',
  },
]

const themeIcon = computed(
  () => themes.find((x) => x.value === appStore.theme)?.icon || 'mdi-theme-light-dark',
)

function cycleTheme() {
  const index = themes.findIndex((x) => x.value === appStore.theme)
  const newIndex = (index + 1) % themes.length
  appStore.theme = themes[newIndex]!.value as 'auto' | boolean
  $q.dark.set(appStore.theme)
}
</script>

<script lang="ts"></script>

<style scoped></style>
