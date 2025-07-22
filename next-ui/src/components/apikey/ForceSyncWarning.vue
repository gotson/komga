<template>
  <v-alert
    type="warning"
    variant="tonal"
    class="mb-4"
  >
    <FormattedMessage :messageDescriptor="message">
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
    </FormattedMessage>
  </v-alert>
</template>

<script setup lang="ts">
import { defineMessage } from 'vue-intl'

const message = defineMessage({
  description: 'API Key for sync warning notice',
  defaultMessage:
    '<ul><li>This will delete all sync history for this API key.</li><li>Your Kobo will sync everything on the next sync.</li></ul>',
  id: 'D/yypj',
})
</script>
