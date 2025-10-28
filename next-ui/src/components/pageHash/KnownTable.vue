<template>
  <v-data-table-server
    v-model:sort-by="sortBy"
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
    multi-sort
    mobile-breakpoint="md"
    @update:options="updateOptions"
  >
    <template #top>
      <v-toolbar
        v-if="display.smAndUp.value"
        flat
      >
        <v-toolbar-title>
          <v-icon
            color="medium-emphasis"
            icon="i-mdi:content-copy"
            size="x-small"
            start
          />
          {{
            $formatMessage({
              description: 'Known Duplicate Page Table global header',
              defaultMessage: 'Known Duplicates',
              id: 'I9ub/l',
            })
          }}
        </v-toolbar-title>

        <v-spacer />
        <v-chip-group
          v-model="filterSelect"
          multiple
          class="ms-2"
        >
          <v-chip
            v-for="f in filterOptions"
            :key="f.value"
            :value="f.value"
            :text="f.title"
            filter
            rounded
            color="primary"
          />
        </v-chip-group>
      </v-toolbar>

      <v-select
        v-else
        v-model="filterSelect"
        label="Filter"
        multiple
        :items="filterOptions"
        chips
        closable-chips
        variant="underlined"
        class="px-4"
      />
    </template>

    <template #no-data>
      <EmptyStateNetworkError v-if="error" />
      <template v-else>
        {{
          $formatMessage({
            description: 'Known Duplicate Page Table: shown when table has no data',
            defaultMessage: 'No data found',
            id: '+6YDzS',
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
          :src="pageHashKnownThumbnailUrl(value)"
          lazy-src="@/assets/cover.svg"
          class="my-1"
          :alt="
            $formatMessage({
              description: 'Known Duplicate Page Table: alt description for thumbnail',
              defaultMessage: 'Duplicate page',
              id: 'P+WI/K',
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
        color="primary"
        rounded="lg"
        mandatory
        @update:model-value="(v) => updateHashAction(item, v)"
      >
        <v-btn
          size="small"
          icon="i-mdi:robot"
          :value="PageHashActionEnum.DELETE_AUTO"
          color="success"
        />
        <v-btn
          size="small"
          icon="i-mdi:hand-back-right"
          :value="PageHashActionEnum.DELETE_MANUAL"
          color="warning"
        />
        <v-btn
          size="small"
          icon="i-mdi:cancel"
          :value="PageHashActionEnum.IGNORE"
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
        <v-btn
          :icon="deletionRequests[item.hash]?.success ? 'i-mdi:timer-sand-empty' : 'i-mdi:delete'"
          :color="deletionRequests[item.hash]?.success ? 'info' : 'error'"
          size="small"
          :disabled="
            value == 0 ||
            getPageHashAction(item) !== PageHashActionEnum.DELETE_MANUAL ||
            deletionRequests[item.hash]?.success
          "
          :loading="deletionRequests[item.hash]?.isLoading"
          @click="deletePageHashMatches(item.hash)"
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
import { useMutation, useQuery } from '@pinia/colada'
import { pageHashesKnownQuery } from '@/colada/page-hashes'
import type { components } from '@/generated/openapi/komga'
import { getFileSize } from '@/utils/utils'
import { pageHashKnownThumbnailUrl } from '@/api/images'
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

const sortBy = ref<SortItem[]>([{ key: 'deleteSize', order: 'desc' }])

//region headers
const headers = [
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: thumbnail',
      defaultMessage: 'Thumbnail',
      id: 'm9h2wk',
    }),
    key: 'hash',
    sortable: false,
  },
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: action',
      defaultMessage: 'Action',
      id: 'XM8VQH',
    }),
    key: 'action',
    value: (item: components['schemas']['PageHashKnownDto']) => {
      return getPageHashAction(item)
    },
  },
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: match count',
      defaultMessage: 'Matches',
      id: 'xfThoX',
    }),
    key: 'matchCount',
  },
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: size',
      defaultMessage: 'Size',
      id: 'syF0Ap',
    }),
    key: 'size',
  },
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: delete count',
      defaultMessage: 'Delete count',
      id: 'QM5+gW',
    }),
    key: 'deleteCount',
  },
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: space saved',
      defaultMessage: 'Space saved',
      id: 'WY7aQf',
    }),
    key: 'deleteSize',
    value: (item: components['schemas']['PageHashKnownDto']) => (item.size || 0) * item.deleteCount,
  },
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: creation date',
      defaultMessage: 'Created',
      id: 'EzOmuX',
    }),
    key: 'created',
  },
  {
    title: intl.formatMessage({
      description: 'Known Duplicate Page Table header: modified date',
      defaultMessage: 'Modified',
      id: 'REmxIM',
    }),
    key: 'lastModified',
  },
] as const
//endregion

//region Filtering
const filterSelect = ref<string[]>([
  PageHashActionEnum.DELETE_AUTO,
  PageHashActionEnum.DELETE_MANUAL,
])
const filterOptions = [
  {
    title: intl.formatMessage(pageHashActionMessages[PageHashActionEnum.DELETE_AUTO]),
    value: PageHashActionEnum.DELETE_AUTO,
  },
  {
    title: intl.formatMessage(pageHashActionMessages[PageHashActionEnum.DELETE_MANUAL]),
    value: PageHashActionEnum.DELETE_MANUAL,
  },
  {
    title: intl.formatMessage(pageHashActionMessages[PageHashActionEnum.IGNORE]),
    value: PageHashActionEnum.IGNORE,
  },
]
//endregion

const pageRequest = ref<PageRequest>(new PageRequest())

const { data, isLoading, error } = useQuery(pageHashesKnownQuery, () => ({
  ...pageRequest.value,
  actions: filterSelect.value,
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
      src: pageHashKnownThumbnailUrl(hash),
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

//region Manual deletion
const deletionRequests = ref<Record<string, { success?: boolean; isLoading: boolean }>>({})
function deletePageHashMatches(hash: string) {
  if (hash in deletionRequests.value && deletionRequests.value[hash]!.success) return

  deletionRequests.value[hash] = {
    isLoading: true,
  }

  useMutation({
    mutation: () =>
      komgaClient.POST('/api/v1/page-hashes/{pageHash}/delete-all', {
        params: { path: { pageHash: hash } },
      }),
  })
    .mutateAsync()
    .then(
      () =>
        (deletionRequests.value[hash] = {
          success: true,
          isLoading: false,
        }),
    )
    .catch((error) => {
      deletionRequests.value[hash] = {
        success: false,
        isLoading: false,
      }
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      })
    })
}
//endregion

//region Update action
const updateRequests = ref<Record<string, PageHashAction>>({})

function getPageHashAction(pageHash: components['schemas']['PageHashKnownDto']): PageHashAction {
  return updateRequests.value[pageHash.hash] || pageHash.action
}

function updateHashAction(
  pageHash: components['schemas']['PageHashKnownDto'],
  newAction: PageHashAction,
) {
  useMutation({
    mutation: () =>
      komgaClient.PUT('/api/v1/page-hashes', {
        body: {
          ...pageHash,
          action: newAction,
        },
      }),
  })
    .mutateAsync()
    .then(() => (updateRequests.value[pageHash.hash] = newAction))
    .catch((error) =>
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      }),
    )
}
//endregion
</script>
<script setup lang="ts"></script>
