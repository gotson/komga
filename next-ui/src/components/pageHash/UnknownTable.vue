<template>
  <v-data-table-server
    v-model="selectedHashes"
    v-model:sort-by="sortBy"
    initial-sort-order="desc"
    :loading="isLoading"
    :items="data?.content"
    :items-length="data?.totalElements || 0"
    :items-per-page-options="[
      { value: 10, title: '10' },
      { value: 25, title: '25' },
      { value: 50, title: '50' },
      { value: 100, title: '100' },
    ]"
    :headers="headers"
    fixed-header
    fixed-footer
    select-strategy="page"
    show-select
    return-object
    item-value="hash"
    multi-sort
    mobile-breakpoint="md"
    @update:options="updateOptions"
  >
    <template #top>
      <v-toolbar flat>
        <v-toolbar-title
          v-if="display.smAndUp.value || (display.xs.value && selectedHashes.length == 0)"
        >
          <v-icon
            color="medium-emphasis"
            icon="i-mdi:content-copy"
            size="x-small"
            start
          />
          {{
            $formatMessage({
              description: 'Unknown Duplicate Page Table global header',
              defaultMessage: 'Unknown Duplicates',
              id: 'XuqK4C',
            })
          }}
        </v-toolbar-title>

        <v-spacer v-if="display.smAndUp.value || (display.xs.value && selectedHashes.length > 0)" />
        <v-btn
          v-if="selectedHashes.length > 0"
          variant="elevated"
          class="mx-2"
          append-icon="i-mdi:menu-down"
        >
          {{
            $formatMessage({
              description: 'Unknown Duplicate Page: selection action button',
              defaultMessage: 'Mark as',
              id: 'lFTdQ+',
            })
          }}

          <v-menu activator="parent">
            <v-list
              density="compact"
              slim
            >
              <v-list-item
                v-for="(item, index) in actionOptions"
                :key="index"
                :title="item.title"
                :prepend-icon="item.icon"
                @click="updateHashActions(selectedHashes, item.value)"
              >
              </v-list-item>
            </v-list>
          </v-menu>
        </v-btn>
      </v-toolbar>
    </template>

    <template #no-data>
      <EmptyStateNetworkError v-if="error" />
      <template v-else>
        {{
          $formatMessage({
            description: 'Unknown Duplicate Page Table: shown when table has no data',
            defaultMessage: 'No data found',
            id: 'hPo41m',
          })
        }}
      </template>
    </template>

    <template #[`item.hash`]="{ value }">
      <div>
        <v-img
          width="200"
          height="200"
          contain
          style="cursor: zoom-in"
          :src="pageHashUnknownThumbnailUrl(value)"
          lazy-src="@/assets/cover.svg"
          class="my-1"
          :alt="
            $formatMessage({
              description: 'Unknown Duplicate Page Table: alt description for thumbnail',
              defaultMessage: 'Duplicate page',
              id: 'IXhDH6',
            })
          "
          @mouseenter="dialogSimple.activator = $event.currentTarget"
          @click="showDialogImage(value)"
        >
          <template #placeholder>
            <div class="d-flex align-center justify-center fill-height">
              <v-progress-circular
                color="grey"
                indeterminate
              />
            </div>
          </template>
        </v-img>
      </div>
    </template>

    <template #[`item.action`]="{ item, value }">
      <v-btn-toggle
        :model-value="value"
        variant="outlined"
        divided
        rounded="lg"
        :disabled="value"
        @update:model-value="(v) => updateHashAction(item, v)"
      >
        <v-btn
          v-tooltip:bottom="
            intl.formatMessage(pageHashActionMessages[PageHashActionEnum.DELETE_AUTO])
          "
          size="small"
          icon="i-mdi:robot"
          :value="PageHashActionEnum.DELETE_AUTO"
          :loading="value === PageHashActionEnum.DELETE_AUTO"
          color="success"
        />
        <v-btn
          v-tooltip:bottom="
            intl.formatMessage(pageHashActionMessages[PageHashActionEnum.DELETE_MANUAL])
          "
          size="small"
          icon="i-mdi:hand-back-right"
          :value="PageHashActionEnum.DELETE_MANUAL"
          :loading="value === PageHashActionEnum.DELETE_MANUAL"
          color="warning"
        />
        <v-btn
          v-tooltip:bottom="intl.formatMessage(pageHashActionMessages[PageHashActionEnum.IGNORE])"
          size="small"
          icon="i-mdi:cancel"
          :value="PageHashActionEnum.IGNORE"
          :loading="value === PageHashActionEnum.IGNORE"
          color=""
        />
      </v-btn-toggle>
    </template>

    <template #[`item.size`]="{ value }">
      {{ getFileSize(value) }}
    </template>

    <template #[`item.deleteSize`]="{ value }">
      {{ getFileSize(value) }}
    </template>

    <template #[`item.matchCount`]="{ item, value }">
      <v-btn-group
        rounded="lg"
        divided
        variant="outlined"
      >
        <v-btn
          :text="value"
          append-icon="i-mdi:image-multiple-outline"
          color=""
          size="small"
          :disabled="value == 0"
          @mouseenter="dialogSimple.activator = $event.currentTarget"
          @click="showDialogMatches(item.hash)"
        />
      </v-btn-group>
    </template>

    <template #[`item.created`]="{ value }">
      <div>{{ $formatDate(value, { dateStyle: 'short' }) }}</div>
      <div>{{ $formatDate(value, { timeStyle: 'short' }) }}</div>
    </template>

    <template #[`item.lastModified`]="{ value }">
      <div>{{ $formatDate(value, { dateStyle: 'short' }) }}</div>
      <div>{{ $formatDate(value, { timeStyle: 'short' }) }}</div>
    </template>
  </v-data-table-server>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { PageRequest, type SortItem } from '@/types/PageRequest'
import { useMutation, useQuery, useQueryCache } from '@pinia/colada'
import { pageHashesUnknownQuery, QUERY_KEYS_PAGE_HASHES } from '@/colada/page-hashes'
import type { components } from '@/generated/openapi/komga'
import { getFileSize } from '@/utils/utils'
import { pageHashUnknownThumbnailUrl } from '@/api/images'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useDisplay } from 'vuetify'
import { VImg } from 'vuetify/components'
import MatchTable from '@/components/pageHash/MatchTable.vue'
import { type ErrorCause, komgaClient } from '@/api/komga-client'
import {
  type PageHashAction,
  PageHashActionEnum,
  pageHashActionMessages,
} from '@/types/PageHashAction'
import { useMessagesStore } from '@/stores/messages'
import { commonMessages } from '@/utils/i18n/common-messages'

const intl = useIntl()
const display = useDisplay()
const messagesStore = useMessagesStore()
const { simple: dialogSimple } = storeToRefs(useDialogsStore())

const selectedHashes = ref<components['schemas']['PageHashUnknownDto'][]>([])
const sortBy = ref<SortItem[]>([{ key: 'matchCount', order: 'desc' }])

//region headers
const headers = [
  {
    title: intl.formatMessage({
      description: 'Unknown Duplicate Page Table header: thumbnail',
      defaultMessage: 'Thumbnail',
      id: 'oyeyK/',
    }),
    key: 'hash',
    sortable: false,
  },
  {
    title: intl.formatMessage({
      description: 'Unknown Duplicate Page Table header: action',
      defaultMessage: 'Action',
      id: 'gxZjIe',
    }),
    key: 'action',
    value: (item: components['schemas']['PageHashUnknownDto']) => {
      return getPageHashAction(item)
    },
    sortable: false,
  },
  {
    title: intl.formatMessage({
      description: 'Unknown Duplicate Page Table header: match count',
      defaultMessage: 'Matches',
      id: 'hdoWGT',
    }),
    key: 'matchCount',
  },
  {
    title: intl.formatMessage({
      description: 'Unknown Duplicate Page Table header: size',
      defaultMessage: 'Size',
      id: 'zRDVnR',
    }),
    key: 'size',
  },
] as const
//endregion

const pageRequest = ref<PageRequest>(new PageRequest())

const { data, isLoading, error } = useQuery(pageHashesUnknownQuery, () => ({
  ...pageRequest.value,
}))

function updateOptions({
  page,
  itemsPerPage,
  sortBy,
}: {
  page: number
  itemsPerPage: number
  sortBy: SortItem[]
}) {
  pageRequest.value = PageRequest.FromVuetify(page - 1, itemsPerPage, sortBy)
}

function showDialogImage(hash: string) {
  dialogSimple.value.dialogProps = {
    fullscreen: display.xs.value,
    scrollable: true,
  }
  dialogSimple.value.slot = {
    component: markRaw(VImg),
    props: {
      src: pageHashUnknownThumbnailUrl(hash),
      contain: true,
      style: 'cursor: zoom-out;',
    },
    handlers: {
      click: () => {
        dialogSimple.value.dialogProps.shown = false
      },
    },
  }
}

function showDialogMatches(hash: string) {
  dialogSimple.value.dialogProps = {
    fullscreen: display.xs.value,
    scrollable: true,
  }
  dialogSimple.value.slot = {
    component: markRaw(MatchTable),
    props: {
      modelValue: hash,
    },
  }
}

//region Update action
const updateRequests = ref<Record<string, PageHashAction>>({})

function getPageHashAction(
  pageHash: components['schemas']['PageHashUnknownDto'],
): PageHashAction | undefined {
  return updateRequests.value[pageHash.hash]
}

async function updateHashAction(
  pageHash: components['schemas']['PageHashUnknownDto'],
  newAction: PageHashAction,
  invalidateCache: boolean = true,
) {
  updateRequests.value[pageHash.hash] = newAction

  return useMutation({
    mutation: () =>
      komgaClient.PUT('/api/v1/page-hashes', {
        body: {
          ...pageHash,
          action: newAction,
        },
      }),
  })
    .mutateAsync()
    .then(() => {
      if (selectedHashes.value.includes(pageHash))
        selectedHashes.value.splice(selectedHashes.value.indexOf(pageHash), 1)
      if (invalidateCache)
        void useQueryCache().invalidateQueries({ key: [QUERY_KEYS_PAGE_HASHES.unknown] })
    })
    .catch((error) =>
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      }),
    )
}

async function updateHashActions(
  pageHashes: components['schemas']['PageHashUnknownDto'][],
  newAction: PageHashAction,
) {
  const updates = pageHashes.map((it) => updateHashAction(it, newAction, false))
  await Promise.allSettled(updates)

  void useQueryCache().invalidateQueries({ key: [QUERY_KEYS_PAGE_HASHES.unknown] })
}

const actionOptions = [
  {
    title: intl.formatMessage(pageHashActionMessages[PageHashActionEnum.DELETE_AUTO]),
    value: PageHashActionEnum.DELETE_AUTO,
    icon: 'i-mdi:robot',
  },
  {
    title: intl.formatMessage(pageHashActionMessages[PageHashActionEnum.DELETE_MANUAL]),
    value: PageHashActionEnum.DELETE_MANUAL,
    icon: 'i-mdi:hand-back-right',
  },
  {
    title: intl.formatMessage(pageHashActionMessages[PageHashActionEnum.IGNORE]),
    value: PageHashActionEnum.IGNORE,
    icon: 'i-mdi:cancel',
  },
]
//endregion
</script>
<script setup lang="ts"></script>
