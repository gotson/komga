<template>
  <template v-if="buildVersion">
    <v-badge
      dot
      color="warning"
      :model-value="isLatestVersion == false"
    >
      <v-btn
        :prepend-icon="mdiTagOutline"
        variant="text"
        color="grey"
        size="small"
        class="text-caption"
        to="/server/updates"
      >
        {{ buildVersion }}
      </v-btn>
    </v-badge>
  </template>
</template>

<script setup lang="ts">
import mdiTagOutline from '~icons/mdi/tag-outline'
import { useAppReleases } from '@/colada/queries/app-releases'

const { buildVersion, isLatestVersion } = useAppReleases()
</script>
