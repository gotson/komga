<template>
  <ImportBooksDirectorySelection
    :loading="isLoading"
    @scan="(directory) => doScan(directory)"
  />
  <ImportBooksTransientBooksTable
    v-if="transientBooks"
    :loading="isLoading"
    :books="transientBooks"
  />
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
