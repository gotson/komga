<template>
  <v-btn
    :loading="isLoading"
    prepend-icon="i-mdi:source-commit"
    variant="text"
    color="grey"
    size="small"
    class="text-caption"
    :href="'https://github.com/gotson/komga/commits/' + commitId"
    target="_blank"
  >
    {{ commitId || $formatMessage(commonMessages.error) }}
  </v-btn>
</template>

<script setup lang="ts">
import { useActuatorInfo } from '@/colada/actuator-info'
import { commonMessages } from '@/utils/i18n/common-messages'

const { commitId, isLoading } = useActuatorInfo()
</script>
