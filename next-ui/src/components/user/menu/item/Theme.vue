<template>
  <v-list-subheader
    :title="
      $formatMessage({
        description: 'User menu: Theme section header',
        defaultMessage: 'Theme',
        id: 'jpdD8R',
      })
    "
  />

  <v-btn-toggle
    v-model="appStore.theme"
    mandatory
    density="compact"
    variant="plain"
    :rounded="0"
    class="d-flex mb-1"
  >
    <v-btn
      v-for="theme in themes"
      :key="theme.value"
      v-ktooltip:bottom="$formatMessage(theme.message)"
      :value="theme.value"
      :icon="theme.icon"
      class="flex-grow-1"
      @click.stop
    />
  </v-btn-toggle>
</template>

<script setup lang="ts">
import { themes } from '@/types/theme'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>
