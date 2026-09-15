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
      class="text-body-small"
      to="/server/updates"
    >
      {{ buildVersion || $formatMessage(commonMessages.error) }}
    </v-btn>
  </v-badge>
</template>

<script setup lang="ts">
import { commonMessages } from '@/utils/i18n/common-messages'
import { useAppReleasesEnriched } from '@/composables/app-releases'

const { buildVersion, isLatestVersion, isLoading } = useAppReleasesEnriched()
</script>
