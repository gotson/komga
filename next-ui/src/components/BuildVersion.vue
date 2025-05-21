<template>
  <template v-if="buildVersion">
    <v-badge
      dot
      color="warning"
      :model-value="isLatestVersion == false"
    >
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
    </v-badge>
  </template>
</template>

<script setup lang="ts">
import {useAppReleases} from '@/colada/queries/app-releases.ts'

const {buildVersion, isLatestVersion} = useAppReleases()
</script>
