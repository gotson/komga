<template>
  <v-skeleton-loader
    v-if="isLoading"
    type="heading, text, paragraph@3"
  />

  <v-empty-state
    v-else-if="error"
    icon="i-mdi:connection"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />

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
        v-tooltip:start="
          $formatMessage({
            description: 'Announcements view: mark all as read button tooltip',
            defaultMessage: 'Mark all as read',
            id: 'da/wb0',
          })
        "
        color="success"
        size="x-large"
        icon="i-mdi:check-all"
      />
    </v-fab>
  </template>
</template>

<script lang="ts" setup>
import { useAnnouncements } from '@/colada/queries/announcements'
import { useMarkAnnouncementsRead } from '@/colada/mutations/mark-announcements-read'
import { commonMessages } from '@/utils/i18n/common-messages'

const { data: announcements, error, unreadCount, isLoading } = useAnnouncements()

const { mutate: markAnnouncementsRead } = useMarkAnnouncementsRead()

function markAllRead() {
  const ids = announcements.value?.items.map((x) => x.id)
  if (ids) markAnnouncementsRead(ids)
}

function markRead(id: string) {
  markAnnouncementsRead([id])
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
