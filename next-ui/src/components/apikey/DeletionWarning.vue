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
  description: 'API Key deletion warning notice',
  defaultMessage:
    'The API key will be deleted from this server.<ul><li>Any application or scripts using this API key will no longer be able to access the Komga API.</li><li>Authentication activity for this API key will be permanently deleted.</li></ul><b>This action cannot be undone.</b>',
  id: 'Y0cG3G',
})
</script>
