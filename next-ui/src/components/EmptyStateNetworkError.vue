<template>
  <v-empty-state
    icon="i-mdi:connection"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />
</template>

<script setup lang="ts">
import { commonMessages } from '@/utils/i18n/common-messages'
</script>
