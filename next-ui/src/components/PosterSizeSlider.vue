<template>
  <v-slider
    v-if="display.smAndUp.value"
    v-model="appStore.gridCardWidth"
    v-ktooltip:bottom="
      $formatMessage({
        description: 'Poster size slider tooltip',
        defaultMessage: 'Poster size',
        id: 'O9hZBS',
      })
    "
    :min="130"
    :max="200"
    color="surface-darken"
    hide-details
    thumb-size="15"
    class="poster-slider flex-grow-0 flex-shrink-0"
  />
</template>

<script setup lang="ts">
import { useDisplay } from 'vuetify'
import { useAppStore } from '@/stores/app'

const display = useDisplay()
const appStore = useAppStore()
</script>

<script lang="ts"></script>

<style scoped>
.poster-slider {
  width: 80px;
  min-width: 80px;
  max-width: 80px;
}
</style>
