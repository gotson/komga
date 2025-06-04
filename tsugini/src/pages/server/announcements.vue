<template>
  <q-card v-if="isLoading">
    <q-card-section>
      <q-skeleton type="text" />
      <q-skeleton
        type="text"
        width="200px"
      />
    </q-card-section>

    <q-card-section>
      <q-skeleton
        height="300px"
        square
      />
    </q-card-section>

    <q-card-actions align="right">
      <q-skeleton type="QBtn" />
    </q-card-actions>
  </q-card>

  <EmptyState
    v-else-if="error"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :sub-title="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
    icon="mdi-connection"
    icon-size="250px"
    avatar-color="grey-4"
  />

  <template v-else-if="announcements">
    <div
      v-for="(item, index) in announcements.items"
      :key="index"
      class="q-py-sm"
    >
      <q-card>
        <q-card-section>
          <div class="text-h3">
            <a
              :href="item.url!"
              target="_blank"
              class="link-underline"
              >{{ item.title }}</a
            >
          </div>
          <div class="text-subtitle1">
            {{ $formatDate(item.date_modified, { dateStyle: 'long' }) }}
          </div>
        </q-card-section>

        <q-card-section>
          <!-- eslint-disable vue/no-v-html -->
          <div
            class="announcement"
            v-html="item.content_html"
          />
          <!-- eslint-enable vue/no-v-html -->
        </q-card-section>

        <q-card-actions align="right">
          <q-btn
            flat
            :color="item._komga?.read ? 'grey' : undefined"
            :disable="item._komga?.read"
            @click="markRead(item.id)"
            >Mark as read</q-btn
          >
        </q-card-actions>
      </q-card>
      <div
        v-if="index == announcements.items.length - 1"
        class="q-mb-xl"
      />
    </div>

    <q-page-sticky
      position="bottom-right"
      :offset="[18, 18]"
      v-show="unreadCount > 0"
    >
      <q-btn
        fab
        icon="mdi-check-all"
        color="positive"
        @click="markAllRead()"
      >
        <q-tooltip class="text-body2">
          {{
            $formatMessage({
              description: 'Announcements view: mark all as read button tooltip',
              defaultMessage: 'Mark all as read',
              id: 'da/wb0',
            })
          }}
        </q-tooltip>
      </q-btn>
    </q-page-sticky>
  </template>
</template>

<script lang="ts" setup>
import { useAnnouncements } from 'colada/queries/announcements'
import { useMarkAnnouncementsRead } from 'colada/mutations/mark-announcements-read'
import { commonMessages } from 'utils/i18n/common-messages'

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
.announcement h2 {
  font-size: 24px;
}
</style>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
