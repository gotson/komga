<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :fullscreen="fullscreen"
    :transition="fullscreen ? 'dialog-bottom-transition' : undefined"
    max-width="600px"
    scrollable
    :aria-label="dialogTitle"
    @after-leave="reset()"
  >
    <template #default="{ isActive }">
      <v-card :title="dialogTitle">
        <template #append>
          <v-icon
            icon="i-mdi:close"
            @click="isActive.value = false"
          />
        </template>

        <v-card-text>
          <v-text-field
            v-model="filterRef"
            :placeholder="
              $formatMessage({
                description: 'Book picker dialog: filter field label',
                defaultMessage: 'Search',
                id: 'G25PY/',
              })
            "
            variant="outlined"
            flat
            hide-details
            autofocus
            prepend-inner-icon="i-mdi:magnify"
          />

          <v-divider class="my-2" />

          <v-data-table
            :items="books"
            :headers="bookTableHeaders"
            :search="filterRef"
            :custom-filter="filterFn"
            fixed-header
            fixed-footer
            style="height: 90%"
          >
            <template #top>
              <v-toolbar
                flat
                :title="
                  $formatMessage({
                    description: 'Book picker dialog: series books table header',
                    defaultMessage: 'Series books',
                    id: 'DK4PsD',
                  })
                "
              >
              </v-toolbar>
            </template>

            <template #[`item.metadata.title`]="{ item }">
              <v-list-item
                class="px-0 cursor-pointer"
                @click="pick(item)"
              >
                <template #prepend>
                  <v-img
                    width="52"
                    height="75"
                    contain
                    :src="bookThumbnailUrl(item.id)"
                    lazy-src="@/assets/cover.svg"
                    class="me-2"
                  />
                </template>

                <v-list-item-title>{{ item.metadata.title }}</v-list-item-title>
                <v-list-item-subtitle v-if="item.metadata.releaseDate"
                  >{{
                    $formatDate(item.metadata.releaseDate, { dateStyle: 'long', timeZone: 'UTC' })
                  }}
                </v-list-item-subtitle>
              </v-list-item>
            </template>
          </v-data-table>
        </v-card-text>
      </v-card>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { bookThumbnailUrl } from '@/api/images'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const showDialog = defineModel<boolean>('dialog', { required: false })

const {
  filter = '',
  books = [],
  fullscreen = undefined,
  activator = undefined,
} = defineProps<{
  filter?: string
  books?: components['schemas']['BookDto'][]
  fullscreen?: boolean
  activator?: Element | string
}>()

const emit = defineEmits<{
  selectedBook: [book: components['schemas']['BookDto']]
}>()

const filterRef = ref<string>(filter)

// sync the ref if the prop changes
watch(
  () => filter,
  (it) => (filterRef.value = it),
)

function pick(selectedBook: components['schemas']['BookDto']) {
  emit('selectedBook', selectedBook)
  showDialog.value = false
}

function reset() {
  filterRef.value = filter
}

const dialogTitle = intl.formatMessage({
  description: 'Book picker dialog: title',
  defaultMessage: 'Select book',
  id: 'SZHxy4',
})

function filterFn(
  value: string,
  query: string,
  item?: { raw: components['schemas']['BookDto'] },
): boolean | number | [number, number] | [number, number][] {
  return (
    item?.raw.metadata.title.includes(query) ||
    item?.raw.metadata.number.includes(query) ||
    item?.raw.metadata.releaseDate?.includes(query) ||
    false
  )
}

const bookTableHeaders = [
  {
    title: intl.formatMessage({
      description: 'Book picker dialog: series books table header: number',
      defaultMessage: 'Number',
      id: 'EIYfj+',
    }),
    key: 'metadata.number',
  },
  {
    title: intl.formatMessage({
      description: 'Book picker dialog: series books table header: book details',
      defaultMessage: 'Details',
      id: 'yrE0Rx',
    }),
    key: 'metadata.title',
  },
]
</script>
<script setup lang="ts"></script>
