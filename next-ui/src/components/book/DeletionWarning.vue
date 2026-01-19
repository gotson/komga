<template>
  <v-alert
    type="warning"
    variant="tonal"
    class="mb-4"
  >
    <FormattedMessage :message-descriptor="message">
      <template #ul="Content">
        <ul class="ps-8">
          <component :is="Content" />
        </ul>
      </template>
      <template #li="Content">
        <li>
          <component :is="Content" />
        </li>
      </template>
      <template #b="Content">
        <div class="font-weight-bold mt-4">
          <component :is="Content" />
        </div>
      </template>
    </FormattedMessage>
  </v-alert>
</template>

<script setup lang="ts">
import { defineMessage } from 'vue-intl'

const message = defineMessage({
  description: 'Series files deletion warning notice',
  defaultMessage:
    'The book files will be deleted<ul><li>Book media file will be deleted from disk.</li><li>Book sidecar files will be deleted from disk.</li><li>Book will de deleted, along with its metadata and read progress.</li></ul><b>This action cannot be undone.</b>',
  id: 'sDGhrD',
})
</script>
