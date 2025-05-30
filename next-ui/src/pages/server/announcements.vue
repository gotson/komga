<template>
  <v-skeleton-loader
    v-if="isLoading"
    type="heading, text, paragraph@3"
  />

  <v-empty-state
    v-else-if="error"
    icon="mdi-connection"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />

  <template v-else-if="announcements">
    <div
      v-for="(item, index) in announcements.items"
      :key="index"
    >
      <v-row
        justify="space-between"
        align="center"
      >
        <v-col cols="auto">
          <div class="ml-n2">
            <a
              :href="item.url"
              target="_blank"
              class="text-h3 font-weight-medium link-underline"
            >{{ item.title }}</a>
          </div>
          <div class="mt-2 subtitle-1">
            {{ $formatDate(item.date_modified, {dateStyle: 'long'}) }}
          </div>
        </v-col>
        <v-col cols="auto">
          <v-tooltip
            :text="$formatMessage({
              description: 'Announcements view: mark as read button tooltip',
              defaultMessage: 'Mark as read',
              id: 'sUSVQS'
            })"
            :disabled="item._komga?.read"
          >
            <template #activator="{ props }">
              <v-fab
                v-bind="props"
                icon="mdi-check"
                elevation="3"
                color="success"
                variant="outlined"
                size="small"
                :disabled="item._komga?.read"
                @click="markRead(item.id)"
              />
            </template>
          </v-tooltip>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <!-- eslint-disable vue/no-v-html -->
          <div
            class="announcement"
            v-html="item.content_html"
          />
          <!-- eslint-enable vue/no-v-html -->
        </v-col>
      </v-row>
      <v-divider
        v-if="index != announcements.items.length - 1"
        class="my-8"
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
        v-tooltip:start="$formatMessage({
          description: 'Announcements view: mark all as read button tooltip',
          defaultMessage: 'Mark all as read',
          id: 'da/wb0'
        })"
        color="success"
        size="x-large"
        icon="mdi-check-all"
      />
    </v-fab>
  </template>
</template>

<script lang="ts" setup>
import {useAnnouncements} from '@/colada/queries/announcements.ts'
import {useMarkAnnouncementsRead} from '@/colada/mutations/mark-announcements-read.ts'
import {commonMessages} from '@/utils/common-messages.ts'

const {data: announcements, error, unreadCount, isLoading} = useAnnouncements()

const {mutate: markAnnouncementsRead} = useMarkAnnouncementsRead()

function markAllRead() {
  const ids = announcements.value?.items.map(x => x.id)
  if(ids) markAnnouncementsRead(ids)
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
