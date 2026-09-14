<template>
  <slot />
</template>

<script setup lang="ts">
import type { BookImport } from '@/types/BookImport'
import { MediaStatus } from '@/types/MediaStatus'
import { useQuery } from '@pinia/colada'
import { transientBookAnalyze } from '@/colada/transient-books'
import { watchImmediate } from '@vueuse/core'
import { seriesDetailQuery } from '@/colada/series'
import { bookListQuery, bookPagesQuery } from '@/colada/books'
import { PageRequest } from '@/types/PageRequest'
import type { BookDto, PageDto, SeriesDto, TransientBookDto } from '@/generated/openapi'

const { bookImport } = defineProps<{ bookImport: BookImport }>()

const emit = defineEmits<{
  'update:transientBook': [book: TransientBookDto]
  'update:series': [series: SeriesDto]
  'update:seriesBooks': [books: BookDto[]]
  'update:upgradeBook': [book: BookDto | undefined]
  'update:upgradeBookPages': [pages: PageDto[]]
}>()

// analyze book if status is UNKNOWN
const { data: transientBookAnalyzed } = useQuery(() => ({
  ...transientBookAnalyze({
    transientBookId: bookImport.transientBook.id,
  }),
  enabled: bookImport.transientBook.status === MediaStatus.Unknown,
}))
watchImmediate(transientBookAnalyzed, (val) => {
  if (val) {
    emit('update:transientBook', val)
  }
})

// fetch series
const { data: seriesDetail } = useQuery(() => ({
  ...seriesDetailQuery({
    seriesId: bookImport.transientBook.seriesId || 'absent',
  }),
  enabled: !!bookImport.transientBook.seriesId,
}))
watchImmediate(seriesDetail, (val) => {
  if (val) {
    emit('update:series', val)
  }
})

// fetch series books
const { data: seriesBooks } = useQuery(() => ({
  ...bookListQuery({
    search: {
      condition: {
        seriesId: { operator: 'Is', value: bookImport.series?.id || 'absent' },
      },
    },
    pageRequest: PageRequest.Unpaged(),
  }),
  enabled: !!bookImport.series,
}))
watchImmediate(seriesBooks, (val) => {
  if (val) {
    emit('update:seriesBooks', val.content ?? [])
  }
})

// fetch upgrade book pages
const { data: upgradeBookPages } = useQuery(() => ({
  ...bookPagesQuery({ bookId: bookImport.upgradeBook?.id || 'absent' }),
  enabled: !!bookImport.upgradeBook,
}))
watchImmediate(upgradeBookPages, (val) => {
  if (val) {
    emit('update:upgradeBookPages', val)
  }
})

// auto-assign upgrade book
watch(
  [() => bookImport.series, () => bookImport.seriesBooks, () => bookImport.transientBook.number],
  ([series, seriesBooks, transientBookNumber], [oldSeries]) => {
    // unset book if series changed
    if (series && oldSeries && series.id !== oldSeries.id) {
      emit('update:upgradeBook', undefined)
    }
    // if series is oneshot, assign the only book
    else if (series?.oneshot && seriesBooks && seriesBooks.at(0)) {
      emit('update:upgradeBook', seriesBooks.at(0))
    }
    // if the transient book has a matched number, use that
    else if (transientBookNumber) {
      const book = seriesBooks?.find((it) => it.metadata.numberSort === transientBookNumber)
      emit('update:upgradeBook', book)
    }
  },
  {
    immediate: true,
  },
)
</script>
