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
  description: 'User deletion warning notice',
  defaultMessage:
    'The user account will be deleted from this server.<ul><li>The read progress for this user account will be permanently deleted.</li><li>Authentication activity for this user will be permanently deleted.</li></ul><b>This action cannot be undone.</b>',
  id: 'q7gxLw',
})
</script>
