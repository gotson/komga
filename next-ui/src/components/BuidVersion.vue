<template>
  <template v-if="buildVersion">
    <v-btn
      prepend-icon="mdi-tag-outline"
      variant="text"
      color="grey"
      size="small"
      class="text-caption"
      to="/server/updates"
    >
      {{ buildVersion }}
    </v-btn>
  </template>
</template>

<script setup lang="ts">
import {useBuildVersion} from '@/composables/buid-version.ts'

const {buildVersion} = useBuildVersion()
</script>
