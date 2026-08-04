<template>
  <FilterIncludeExclude
    v-model="model"
    :label="
      $formatMessage({
        description: 'Filter: unavailable',
        defaultMessage: 'Unavailable',
        id: '+pyj1u',
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
