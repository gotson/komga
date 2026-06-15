<template>
  <v-container>
    <v-row>
      <v-col
        cols="6"
        sm="3"
      >
        <v-img
          cover
          position="top"
          :src="seriesPosterUrl(series.id)"
          lazy-src="@/assets/cover.svg"
          aspect-ratio="0.7071"
          rounded
          :max-width="posterMaxWidth"
        >
          <template #placeholder>
            <div class="d-flex align-center justify-center fill-height">
              <v-progress-circular
                color="grey"
                indeterminate
              />
            </div>
          </template>

          <!--  This will just show lazy-src without the v-progress  -->
          <template #error></template>

          <!--  Top-right icon  -->
          <div
            v-if="isRead || unreadCount"
            class="top-0 right-0 position-absolute translucent text-white px-2 py-1 font-weight-bold text-body-small"
            style="border-bottom-left-radius: 4px"
          >
            <v-icon
              v-if="isRead"
              icon="i-mdi:check"
            />
            <template v-else>{{ unreadCount }}</template>
          </div>
        </v-img>

        <v-alert
          v-if="isRead || bookOnDeck"
          :icon="isRead ? 'i-mdi:check' : undefined"
          class="mt-1 text-center text-body-small"
          :max-width="posterMaxWidth"
        >
          <template v-if="bookOnDeck">{{
            $formatMessage(
              {
                description: 'Series view: book on deck',
                defaultMessage: 'On deck — #{number}',
                id: '5cbjLE',
              },
              { number: bookOnDeck.metadata.number },
            )
          }}</template>
          <template v-if="isRead">{{
            $formatMessage({
              description: 'Series view: read indicator',
              defaultMessage: 'Read',
              id: 'l7mpQK',
            })
          }}</template>
        </v-alert>
      </v-col>

      <v-col
        cols="6"
        sm="9"
      >
        <v-container class="pa-0">
          <v-row>
            <v-col>
              <div class="text-headline-small">{{ series.metadata.title }}</div>
            </v-col>
          </v-row>

          <v-row density="compact">
            <v-col>
              <SimpleDataTable :rows="alternateTitles" />
            </v-col>
          </v-row>

          <v-row
            density="comfortable"
            align="baseline"
          >
            <v-col cols="auto">
              <div class="text-body-medium">
                <span v-if="series.metadata.totalBookCount">{{
                  $formatMessage(
                    {
                      description: 'Series view: count of books in series with total count',
                      defaultMessage: `{total, plural,
one {{count} / # book}
other {{count} / # books}
}`,
                      id: 'VhMXcu',
                    },
                    { count: series.booksCount, total: series.metadata.totalBookCount },
                  )
                }}</span>
                <span v-else>{{
                  $formatMessage(
                    {
                      description: 'Series view: count of books in series',
                      defaultMessage: `{count, plural,
one {# book}
other {# books}
}`,
                      id: '4X3RAp',
                    },
                    { count: series.booksCount },
                  )
                }}</span>
              </div>
            </v-col>

            <v-col
              v-if="series.booksMetadata.releaseDate"
              cols="auto"
            >
              <div class="text-body-medium">
                {{
                  $formatDate(series.booksMetadata.releaseDate, {
                    year: 'numeric',
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
          <SeriesViewActions :series="series" />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <div class="d-flex ga-2">
            <v-chip
              v-if="seriesStatus"
              size="small"
              rounded
              label
              :text="$formatMessage(seriesStatusMessages[seriesStatus])"
            />
            <v-chip
              v-if="series.metadata.language"
              size="small"
              rounded
              label
              :text="languageDisplayNames.of(series.metadata.language)"
            />
            <v-chip
              v-if="series.metadata.ageRating"
              size="small"
              rounded
              label
              :text="
                $formatMessage(
                  {
                    description: 'Series view: age rating chip',
                    defaultMessage: '{rating}+',
                    id: 'rnClur',
                  },
                  { rating: series.metadata.ageRating },
                )
              "
            />
            <v-chip
              v-if="readingDirection"
              size="small"
              rounded
              label
              :text="$formatMessage(readingDirectionMessages[readingDirection])"
            />
          </div>
        </v-col>
      </v-row>

      <v-row density="compact">
        <v-col>
          <v-alert
            v-if="series.deleted"
            type="error"
            variant="tonal"
            :text="
              $formatMessage({
                description: 'Series view: files deleted',
                defaultMessage: 'The series files could not be found',
                id: 'Ku7NJ+',
              })
            "
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <div
            v-if="!series.metadata.summary && series.booksMetadata.summary"
            class="text-label-medium"
          >
            {{
              $formatMessage(
                {
                  description: 'Series view: summary from book label',
                  defaultMessage: 'Summary from book {number}:',
                  id: 'MEWT0D',
                },
                { number: series.booksMetadata.summaryNumber },
              )
            }}
          </div>
          <ReadMore :text="series.metadata.summary || series.booksMetadata.summary" />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <SimpleDataTable :rows="tableRows" />
        </v-col>
      </v-row>

      <v-row density="compact">
        <v-col cols="auto">
          <v-btn
            variant="text"
            size="small"
            text="Show all"
            @mouseenter="dialogSimple.activator = $event.currentTarget"
            @click="showDialogExtra()"
          />
        </v-col>
      </v-row>
    </v-container>
  </Teleport>
</template>

<script setup lang="ts">
import { seriesPosterUrl } from '@/api/images'
import type { components } from '@/generated/openapi/komga'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import SimpleDataTable, { type TableRow } from '@/components/SimpleDataTable.vue'
import { contributorsRolesMessages } from '@/types/referential'
import { createOrderCompareFn } from '@/functions/sort'
import { useSeries } from '@/composables/series/useSeries'
import { readingDirectionMessages } from '@/types/ReadingDirection'
import { languageDisplayNames } from '@/utils/i18n/locale-helper'
import { seriesStatusMessages } from '@/types/SeriesStatus'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useSeriesBooks } from '@/composables/series/useSeriesBooks'

const intl = useIntl()
const display = useDisplay()
const id = useId()
const posterMaxWidth = 220

const props = defineProps<{
  series: components['schemas']['SeriesDto']
}>()

const { unreadCount, isRead, readingDirection, seriesStatus } = useSeries(props.series)
const { getFirstBookInSeries } = useSeriesBooks(props.series.id)

const bookOnDeck = ref<components['schemas']['BookDto'] | undefined>(undefined)
void getFirstBookInSeries(true).then((data) => (bookOnDeck.value = data))

const alternateTitles = computed(() =>
  props.series.metadata.alternateTitles.map((it) => ({
    header: it.label,
    data: it.title,
  })),
)

const allRows = computed(() => {
  const rows = {} as Record<string, TableRow>

  if (props.series.booksMetadata.authors.length > 0)
    Object.entries(Object.groupBy(props.series.booksMetadata.authors, (it) => it.role))
      .toSorted(createOrderCompareFn(Object.keys(contributorsRolesMessages), ([role]) => role))
      .forEach(([role, contributor]) => {
        rows[role] = {
          header: contributorsRolesMessages?.[role]
            ? intl.formatMessage(contributorsRolesMessages?.[role])
            : role,
          data: contributor!.map((it) => ({ text: it.name })),
        }
      })

  if (props.series.metadata.publisher)
    rows['publisher'] = {
      header: intl.formatMessage({
        description: 'Series view table: publisher header',
        defaultMessage: 'Publisher',
        id: 'OLqBQc',
      }),
      data: [{ text: props.series.metadata.publisher }],
    }

  if (props.series.metadata.genres.length > 0)
    rows['genres'] = {
      header: intl.formatMessage({
        description: 'Series view table: genre header',
        defaultMessage: 'Genre',
        id: 'r5O+/d',
      }),
      data: props.series.metadata.genres.map((it) => ({ text: it })),
    }

  if (props.series.metadata.tags.length > 0)
    rows['tags'] = {
      header: intl.formatMessage({
        description: 'Series view table: tags header',
        defaultMessage: 'Tags',
        id: '6UXlVe',
      }),
      data: props.series.metadata.tags.map((it) => ({ text: it })),
    }
  if (props.series.booksMetadata.tags.length > 0)
    rows['bookTags'] = {
      header: intl.formatMessage({
        description: 'Series view table: book tags header',
        defaultMessage: 'Book tags',
        id: 'Thjcar',
      }),
      data: props.series.booksMetadata.tags.map((it) => ({ text: it })),
    }

  if (props.series.metadata.links.length > 0)
    rows['links'] = {
      header: intl.formatMessage({
        description: 'Series view table: links header',
        defaultMessage: 'Links',
        id: 'fbEqBB',
      }),
      data: props.series.metadata.links.map((it) => ({ text: it.label, href: it.url })),
    }
  rows['filePath'] = {
    header: intl.formatMessage({
      description: 'Series view table: file path header',
      defaultMessage: 'File path',
      id: '+mJGIg',
    }),
    data: props.series.url,
  }
  rows['created'] = {
    header: intl.formatMessage({
      description: 'Series view table: date created header',
      defaultMessage: 'Created',
      id: 'dx9s7S',
    }),
    data: intl.formatDate(props.series.created, { dateStyle: 'medium', timeStyle: 'short' }),
  }
  rows['modified'] = {
    header: intl.formatMessage({
      description: 'Series view table: date last modified header',
      defaultMessage: 'Last modified',
      id: 'Y4xJBN',
    }),
    data: intl.formatDate(props.series.lastModified, { dateStyle: 'medium', timeStyle: 'short' }),
  }

  return rows
})

const displayDefault = ['writer', 'penciller', 'publisher', 'genre', 'tags', 'links']
const tableRows = computed(() =>
  Object.entries(allRows.value)
    .filter(([key]) => displayDefault.includes(key))
    .map(([, value]) => value),
)

const { simple: dialogSimple } = storeToRefs(useDialogsStore())

function showDialogExtra() {
  dialogSimple.value.dialogProps = {
    fullscreen: display.xs.value,
    scrollable: true,
    maxWidth: 900,
  }
  dialogSimple.value.slot = {
    component: markRaw(SimpleDataTable),
    props: {
      rows: Object.values(allRows.value),
    },
  }
}
</script>
