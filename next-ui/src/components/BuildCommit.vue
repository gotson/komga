<template>
  <template v-if="commitId">
    <v-btn
      prepend-icon="mdi-source-commit"
      variant="text"
      color="grey"
      size="small"
      class="text-caption"
      :href="'https://github.com/gotson/komga/commits/' + commitId"
      target="_blank"
    >
      {{ commitId }}
    </v-btn>
  </template>
</template>

<script setup lang="ts">
import { useActuatorInfo } from '@/colada/queries/actuator-info'

const { commitId } = useActuatorInfo()
</script>
