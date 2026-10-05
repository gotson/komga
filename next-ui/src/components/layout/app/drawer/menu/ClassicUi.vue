<template>
  <v-list-item
    :title="
      $formatMessage({
        description: 'Drawer menu for webui link',
        defaultMessage: 'Open classic UI',
        id: '5nswuX',
      })
    "
    prepend-icon="i-mdi:ab-testing"
    :href="`${ApiBaseUrl.noSlash}/classic`"
  />
</template>

<script setup lang="ts">
import { ApiBaseUrl } from '@/api/base'
</script>
