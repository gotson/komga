<template>
  <v-menu>
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        icon="i-mdi:view-grid-plus"
        :aria-label="
          $formatMessage({
            description: 'Page size selector button: aria-label',
            defaultMessage: 'page size selector',
            id: '2EMvSm',
          })
        "
      />
    </template>

    <v-list
      :selected="[pageSize]"
      color="primary"
    >
      <v-list-item
        v-for="size in sizes"
        :key="size"
        :title="size"
        :value="size"
        @click="pageSize = size"
      />

      <v-list-item
        v-if="allowUnpaged"
        :title="
          $formatMessage({
            description: 'Page size selector: unpaged option',
            defaultMessage: 'All',
            id: 'MC2JtF',
          })
        "
        value="unpaged"
        @click="pageSize = 'unpaged'"
      />
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import type { PageSize } from '@/types/page'

const pageSize = defineModel<PageSize>({ required: true })

const { sizes = [20, 50, 100, 200, 500], allowUnpaged = false } = defineProps<{
  sizes?: number[]
  allowUnpaged?: boolean
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
