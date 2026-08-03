<template>
  <EmptyStateNetworkError v-if="error" />

  <template v-else>
    <v-list
      :disabled="isLoading"
      elevation="1"
    >
      <v-progress-linear
        v-if="isLoading"
        indeterminate
        height="3"
        class="position-absolute top-0"
      />

      <v-text-field
        v-model="selectedPath"
        readonly
        label="Selected path"
        variant="solo"
        flat
        hide-details
      />

      <v-divider class="my-2" />

      <template v-if="directoryListing">
        <!--  Parent directory  -->
        <v-list-item
          v-if="directoryListing.parent || directoryListing.parent === ''"
          :title="
            $formatMessage({
              description: 'File browser: parent directory',
              defaultMessage: 'Parent',
              id: 'B48EcS',
            })
          "
          prepend-icon="i-mdi:arrow-left"
          @click="selectedPath = directoryListing.parent"
        />

        <!--  Directory listing  -->
        <v-list-item
          v-for="(dir, index) in directoryListing.directories"
          :key="index"
          :title="dir.name"
          prepend-icon="i-mdi:folder"
          @click="selectedPath = dir.path"
        />
      </template>
    </v-list>
  </template>
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { komgaGetDirectoryListing } from '@/generated/openapi'
import { isApiErrorWithCause } from '@/api/komga-client'

const selectedPath = defineModel<string>({ required: false, default: '' })

const {
  data: directoryListing,
  isLoading,
  error,
} = useQuery({
  key: () => ['filesystem', selectedPath.value],
  query: () =>
    komgaGetDirectoryListing({
      body: {
        path: selectedPath.value,
        showFiles: false,
      },
    }).catch((error) => {
      // if we hit an error, clear the input which may be erroneous
      // the API will be retried automatically
      if (isApiErrorWithCause(error) && error.cause.status === 400) {
        selectedPath.value = ''
      }
    }),
  placeholderData: (previousData) => previousData,
})
</script>
