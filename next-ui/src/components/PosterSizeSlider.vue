<template>
  <v-slider
    v-if="display.smAndUp.value"
    v-model="appStore.gridCardWidth"
    :min="130"
    :max="200"
    color="surface-darken"
    hide-details
    thumb-size="15"
    max-width="80"
  />
</template>

<script setup lang="ts">
import { useDisplay } from 'vuetify'
import { useAppStore } from '@/stores/app'

const display = useDisplay()
const appStore = useAppStore()
</script>

<script lang="ts"></script>

<style scoped></style>
