<template>
  <v-card>
    <template #title>
      <a
        :href="item.url"
        target="_blank"
        class="text-h3 font-weight-medium link-underline"
        >{{ item.title }}</a
      >
    </template>
    <template #subtitle>
      {{ $formatDate(item.date_modified, { dateStyle: 'long' }) }}
    </template>

    <template #text>
      <!-- eslint-disable vue/no-v-html -->
      <div
        class="announcement"
        v-html="item.content_html"
      />
      <!-- eslint-enable vue/no-v-html -->
    </template>

    <template #actions>
      <v-spacer />
      <v-btn
        :text="
          $formatMessage({
            description: 'Announcements view: mark as read button tooltip',
            defaultMessage: 'Mark as read',
            id: 'sUSVQS',
          })
        "
        :disabled="item._komga?.read"
        @click="emit('markRead', item.id)"
      />
    </template>
  </v-card>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'

const { item } = defineProps<{
  item: components['schemas']['ItemDto']
}>()
const emit = defineEmits<{
  markRead: [id: string]
}>()
</script>

<style lang="scss">
.announcement p {
  margin-bottom: 16px;
}

.announcement ul {
  padding-left: 24px;
}

.announcement a {
  color: var(--v-anchor-base);
}
</style>
