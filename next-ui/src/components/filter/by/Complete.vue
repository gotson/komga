<template>
  <FilterIncludeExclude
    v-model="model"
    :label="
      $formatMessage({
        description: 'Filter: complete',
        defaultMessage: 'Complete',
        id: 'ZEbDUo',
      })
    "
  />
</template>

<script setup lang="ts">
import { type FilterIncludeExclude } from '@/types/filter'

const model = defineModel<FilterIncludeExclude>({ required: true })
</script>

<script lang="ts"></script>

<style scoped></style>
