<template>
  <v-badge
    dot
    color="warning"
    :model-value="isLatestVersion == false"
  >
    <v-btn
      :loading="isLoading"
      prepend-icon="i-mdi:tag-outline"
      variant="text"
      color="grey"
      size="small"
      class="text-caption"
      to="/server/updates"
    >
      {{ buildVersion || $formatMessage(commonMessages.error) }}
    </v-btn>
  </v-badge>
</template>

<script setup lang="ts">
import { useAppReleases } from '@/colada/queries/app-releases'
import { commonMessages } from '@/utils/i18n/common-messages'

const { buildVersion, isLatestVersion, isLoading } = useAppReleases()
</script>
