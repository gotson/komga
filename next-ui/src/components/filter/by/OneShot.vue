<template>
  <FilterIncludeExclude
    v-model="model"
    :label="
      $formatMessage({
        description: 'Filter: one-shot',
        defaultMessage: 'One-shot',
        id: '4uqafX',
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
