<template>
  <v-container fluid>
    <v-skeleton-loader
      v-if="isPending"
      type="heading, text, paragraph@3"
    />

    <EmptyStateNetworkError v-else-if="error" />

    <template v-else-if="announcements">
      <div
        v-for="(item, index) in announcements.items"
        :key="index"
      >
        <AnnouncementCard
          class="mb-4"
          :item="item"
          @mark-read="markRead"
        />

        <!--  Bottom spacing for the FAB  -->
        <div
          v-if="index == announcements.items.length - 1"
          class="mb-16"
        />
      </div>

      <v-fab
        :active="unreadCount > 0"
        location="bottom right"
        app
        icon
        size="x-large"
        class="ms-n5"
        @click="markAllRead()"
      >
        <!--  Workaround for https://github.com/vuetifyjs/vuetify/issues/21439  -->
        <v-btn
          v-tooltip:start="$formatMessage(markAllReadMessage)"
          color="success"
          size="x-large"
          icon="i-mdi:check-all"
          :aria-label="$formatMessage(markAllReadMessage)"
        />
      </v-fab>
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useAnnouncements, useMarkAnnouncementsRead } from '@/colada/announcements'
import EmptyStateNetworkError from '@/components/EmptyStateNetworkError.vue'
import { defineMessage } from 'vue-intl'

const { data: announcements, error, unreadCount, isPending } = useAnnouncements()

const { mutate: markAnnouncementsRead } = useMarkAnnouncementsRead()

function markAllRead() {
  const ids = announcements.value?.items.map((x) => x.id)
  if (ids) markAnnouncementsRead(ids)
}

function markRead(id: string) {
  markAnnouncementsRead([id])
}

const markAllReadMessage = defineMessage({
  description: 'Announcements view: mark all as read button tooltip',
  defaultMessage: 'Mark all as read',
  id: 'da/wb0',
})
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
