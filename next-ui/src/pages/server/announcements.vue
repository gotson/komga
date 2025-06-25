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
      <v-card class="mb-4">
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
            @click="markRead(item.id)"
          />
        </template>
      </v-card>

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

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
