<template>
  <template v-if="commit">
    <v-btn
      prepend-icon="mdi-source-commit"
      variant="text"
      color="grey"
      size="small"
      class="text-caption"
      :href="'https://github.com/gotson/komga/commits/' + commit"
      target="_blank"
    >
      {{ commit }}
    </v-btn>
  </template>
</template>

<script setup lang="ts">
import {useActuatorInfo} from '@/colada/queries/actuator-info'

const {data} = useActuatorInfo()

const commit = computed(() => data.value?.git?.commit?.id)
</script>
