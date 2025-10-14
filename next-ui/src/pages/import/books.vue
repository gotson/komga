<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4 h-100 h-sm-auto"
  >
    <ImportBooksDirectorySelection
      :loading="isLoading"
      @scan="(directory) => doScan(directory)"
    />
    <ImportBooksTransientBooksTable
      v-if="transientBooks"
      :loading="isLoading"
      :books="transientBooks"
    />
  </v-container>
</template>

<script lang="ts" setup>
import { transientBooksScan } from '@/colada/transient-books'
import { useQuery } from '@pinia/colada'

const scanDirectory = ref<string>('')

const {
  data: transientBooks,
  isLoading,
  refetch,
} = useQuery(transientBooksScan, () => ({
  path: scanDirectory.value,
}))

function doScan(directory: string) {
  scanDirectory.value = directory
  void refetch()
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
