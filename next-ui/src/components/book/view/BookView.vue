<template>
  <v-container fluid>
    <v-row>
      <v-col
        cols="6"
        sm="3"
      >
        <ItemPoster
          :poster-url="bookPosterUrl(book.id)"
          :progress-percent="progressPercent"
          :max-width="posterMaxWidth"
        />

        <v-alert
          v-if="isRead || pagesLeft"
          :icon="isRead ? 'i-mdi:check' : undefined"
          class="mt-1 text-center text-body-small"
          :max-width="posterMaxWidth"
        >
          <template v-if="pagesLeft">{{
            $formatMessage(
              {
                description: 'Book view: number of pages left',
                defaultMessage: '{count} pages left',
                id: 'Z5hsZ9',
              },
              { count: pagesLeft },
            )
          }}</template>
          <template v-if="isRead">{{
            $formatMessage(
              {
                description: 'Book view: date read',
                defaultMessage: 'Read on {readDate}',
                id: 'T3Ofay',
              },
              {
                readDate: intl.formatDate(book.readProgress?.readDate, {
                  dateStyle: 'medium',
                }),
              },
            )
          }}</template>
        </v-alert>
      </v-col>

      <v-col
        cols="6"
        sm="9"
      >
        <v-container class="pa-0">
          <v-row v-if="!book.oneshot">
            <v-col>
              <RouterLink
                :to="{ name: '/series/[id]', params: { id: book.seriesId } }"
                class="text-headline-large link-underline"
                >{{ book.seriesTitle }}</RouterLink
              >
            </v-col>
          </v-row>

          <v-row density="compact">
            <v-col>
              <div class="text-headline-small">{{ book.metadata.title }}</div>
            </v-col>
          </v-row>

          <v-row align="baseline">
            <v-col
              v-if="!book.oneshot"
              cols="auto"
            >
              <div class="text-body-medium">
                {{
                  $formatMessage(
                    {
                      description: 'Book view: book number in series',
                      defaultMessage: 'Book {number}',
                      id: 'O4Nnj5',
                    },
                    { number: book.metadata.number },
                  )
                }}
              </div>
            </v-col>

            <v-col cols="auto">
              <div class="text-body-medium">
                {{
                  $formatMessage(
                    {
                      description: 'Book view: total number of pages',
                      defaultMessage: '{pagesCount} pages',
                      id: 'ZZmdmI',
                    },
                    { pagesCount: book.media.pagesCount },
                  )
                }}
              </div>
            </v-col>
          </v-row>

          <v-row
            v-if="book.metadata.releaseDate"
            density="comfortable"
          >
            <v-col cols="auto">
              <div class="text-body-medium">
                {{
                  $formatDate(book.metadata.releaseDate, {
                    dateStyle: 'long',
                    timeZone: 'UTC',
                  })
                }}
              </div>
            </v-col>
          </v-row>
        </v-container>

        <div :id="`sm-${id}`" />
      </v-col>
    </v-row>

    <div :id="`xs-${id}`" />
  </v-container>

  <Teleport
    :to="`#${display.xs.value ? 'xs' : 'sm'}-${id}`"
    defer
  >
    <v-container class="px-0">
      <v-row>
        <v-col>
          <BookViewActions :book="book" />
        </v-col>
      </v-row>

      <v-row
        v-if="mediaStatus !== MediaStatus.READY || isDeleted"
        density="compact"
      >
        <v-col>
          <v-alert
            v-if="isDeleted"
            type="error"
            variant="tonal"
            :text="
              $formatMessage({
                description: 'Book view: book deleted',
                defaultMessage: 'The book file could not be found',
                id: 'kZ0WlY',
              })
            "
          />

          <v-alert
            v-if="mediaStatus === MediaStatus.ERROR"
            type="error"
            variant="tonal"
          >
            {{
              $formatMessage({
                description: 'Book view: media error',
                defaultMessage: 'The book file could not be analyzed:',
                id: 'sjC8ot',
              })
            }}
            {{ convertErrorCodes(book.media.comment) }}
          </v-alert>

          <v-alert
            v-if="mediaStatus === MediaStatus.UNSUPPORTED"
            type="warning"
            variant="tonal"
            :text="
              $formatMessage({
                description: 'Book view: media unsupported',
                defaultMessage: 'The book file format is not supported',
                id: 'CkUPZe',
              })
            "
          />

          <v-alert
            v-if="mediaStatus === MediaStatus.UNKNOWN"
            type="info"
            variant="tonal"
            :text="
              $formatMessage({
                description: 'Book view: media unknown',
                defaultMessage: 'The book file is yet to be analyzed',
                id: 'pHycdf',
              })
            "
          />

          <v-alert
            v-if="mediaStatus === MediaStatus.OUTDATED"
            type="info"
            variant="tonal"
            :text="
              $formatMessage({
                description: 'Book view: media outdated',
                defaultMessage: 'The book file has changed and will need to be analyzed again',
                id: 'CGRz8d',
              })
            "
          />
        </v-col>
      </v-row>

      <v-row v-if="book.metadata.summary">
        <v-col>
          <ReadMore :text="book.metadata.summary" />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <SimpleDataTable :rows="tableRows" />
        </v-col>
      </v-row>
    </v-container>
  </Teleport>
</template>

<script setup lang="ts">
import { bookPosterUrl } from '@/api/images'
import { useBookReadProgress } from '@/composables/book/useBookReadProgress'

import { useIntl } from 'vue-intl'
import { useBook } from '@/composables/book/useBook'
import { MediaStatus } from '@/types/MediaStatus'
import { useDisplay } from 'vuetify'
import SimpleDataTable, { type TableRow } from '@/components/SimpleDataTable.vue'
import { contributorsRolesMessages } from '@/types/referential'
import { getFileSize } from '@/utils/utils'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
import { createOrderCompareFn } from '@/functions/sort'
import type { BookDto } from '@/generated/openapi'

const intl = useIntl()
const display = useDisplay()
const { convertErrorCodes } = useErrorCodeFormatter()
const id = useId()
const posterMaxWidth = 220

const props = defineProps<{
  book: BookDto
}>()

const { isRead, progressPercent, pagesLeft } = useBookReadProgress(() => props.book)
const { mediaStatus, isDeleted, format } = useBook(() => props.book)

const tableRows = computed(() => {
  const rows: TableRow[] = []

  if (props.book.metadata.authors.length > 0)
    Object.entries(Object.groupBy(props.book.metadata.authors, (it) => it.role))
      .toSorted(createOrderCompareFn(Object.keys(contributorsRolesMessages), ([role]) => role))
      .forEach(([role, contributor]) => {
        rows.push({
          header: contributorsRolesMessages?.[role]
            ? intl.formatMessage(contributorsRolesMessages?.[role])
            : role,
          data: contributor!.map((it) => ({ text: it.name })),
        })
      })

  if (props.book.metadata.tags.length > 0)
    rows.push({
      header: intl.formatMessage({
        description: 'Book view table: tags header',
        defaultMessage: 'Tags',
        id: 'TPX4qo',
      }),
      data: props.book.metadata.tags.map((it) => ({ text: it })),
    })
  if (props.book.metadata.links.length > 0)
    rows.push({
      header: intl.formatMessage({
        description: 'Book view table: links header',
        defaultMessage: 'Links',
        id: 'UfbIyV',
      }),
      data: props.book.metadata.links.map((it) => ({ text: it.label, href: it.url })),
    })
  if (props.book.metadata.isbn)
    rows.push({
      header: intl.formatMessage({
        description: 'Book view table: ISBN header',
        defaultMessage: 'ISBN',
        id: 'VQ/hT4',
      }),
      data: props.book.metadata.isbn,
    })
  if (props.book.media.comment)
    rows.push({
      header: intl.formatMessage({
        description: 'Book view table: file type header',
        defaultMessage: 'File type',
        id: 'QALnuE',
      }),
      data: format.value,
    })
  rows.push({
    header: intl.formatMessage({
      description: 'Book view table: file size header',
      defaultMessage: 'File size',
      id: '1E0Duw',
    }),
    data: getFileSize(props.book.sizeBytes)!,
  })
  rows.push({
    header: intl.formatMessage({
      description: 'Book view table: file path header',
      defaultMessage: 'File path',
      id: 'CE7pPQ',
    }),
    data: props.book.url,
  })
  rows.push({
    header: intl.formatMessage({
      description: 'Book view table: date created header',
      defaultMessage: 'Created',
      id: 'jHJk+v',
    }),
    data: intl.formatDate(props.book.created, { dateStyle: 'medium', timeStyle: 'short' }),
  })
  rows.push({
    header: intl.formatMessage({
      description: 'Book view table: date last modified header',
      defaultMessage: 'Last modified',
      id: 'samiG7',
    }),
    data: intl.formatDate(props.book.lastModified, { dateStyle: 'medium', timeStyle: 'short' }),
  })
  if (props.book.media.comment)
    rows.push({
      header: intl.formatMessage({
        description: 'Book view table: media comment header',
        defaultMessage: 'Media comment',
        id: 'dK/kHO',
      }),
      data: convertErrorCodes(props.book.media.comment),
      dataProps: {
        class: 'text-red font-weight-bold',
      },
    })

  return rows
})
</script>
