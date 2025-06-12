<template>
  <v-btn
    :loading="isLoading"
    :prepend-icon="mdiSourceCommit"
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
import mdiSourceCommit from '~icons/mdi/source-commit'
import { useActuatorInfo } from '@/colada/queries/actuator-info'
import { commonMessages } from '@/utils/i18n/common-messages'

const { commitId, isLoading } = useActuatorInfo()
</script>
