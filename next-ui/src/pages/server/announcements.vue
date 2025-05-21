<template>
  <v-alert
    v-if="error"
    type="error"
    variant="tonal"
  >
    Error loading data
  </v-alert>

  <template v-if="data">
    <div
      v-for="(item, index) in data.items"
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
            {{ item.date_modified }}
          </div>
        </v-col>
        <v-col cols="auto">
          <v-tooltip
            text="Mark as read"
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
        v-if="index != data.items.length - 1"
        class="my-8"
      />
    </div>

    <v-fab
      :active="unreadCount > 0"
      color="success"
      location="bottom right"
      app
      icon="mdi-check-all"
      size="x-large"
      class="ms-n5"
      @click="markAllRead()"
    />
  </template>
</template>

<script lang="ts" setup>
import {useAnnouncements} from '@/colada/queries/announcements.ts'
import {useMarkAnnouncementsRead} from '@/colada/mutations/mark-announcements-read.ts'

const {data, error, unreadCount} = useAnnouncements()

const {mutate} = useMarkAnnouncementsRead()

function markAllRead() {
  const ids = data.value?.items.map(x => x.id)
  if(ids) mutate(ids)
}

function markRead(id: string) {
  mutate([id])
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
