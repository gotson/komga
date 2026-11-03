<template>
  <v-list-item
    :title="
      $formatMessage({
        description: 'User menu: stretch posters',
        defaultMessage: 'Fill poster containers',
        id: 'PUU04H',
      })
    "
  >
    <template #append>
      <v-switch
        v-model="appStore.stretchPoster"
        true-icon="i-mdi:check"
        false-icon="i-mdi:close"
        color="primary"
        inset="material"
        hide-details
        @click.stop
      />
    </template>
  </v-list-item>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>
