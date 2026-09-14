<template>
  <slot />
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { bookListQuery } from '@/colada/books'
import { PageRequest } from '@/types/PageRequest'
import type { BookDto, ReadListRequestBookMatchBookDto } from '@/generated/openapi'
import type { ReadListImportEntry } from '@/types/ReadListImportEntry'

const { entry } = defineProps<{ entry: ReadListImportEntry }>()

const emit = defineEmits<{
  'update:seriesBooks': [books: BookDto[]]
  'update:matchedBook': [book: ReadListRequestBookMatchBookDto | undefined]
}>()

// fetch series books on trigger
const { data: seriesBooks } = useQuery(() => ({
  ...bookListQuery({
    search: {
      condition: {
        seriesId: { operator: 'Is', value: entry.series?.seriesId || 'none' },
      },
    },
    pageRequest: PageRequest.Unpaged(),
  }),
  enabled: entry.shouldFetchSeriesBooks && !!entry.series?.seriesId,
}))
watch(seriesBooks, (val) => {
  if (val) {
    emit('update:seriesBooks', val.content ?? [])
  }
})

// match book if series changes
watch(
  [() => entry.series?.seriesId, () => entry.seriesBooks],
  ([seriesId, books], [oldSeriesId]) => {
    if (seriesId && (!oldSeriesId || seriesId !== oldSeriesId) && books) {
      const matchedBook = books.find((it) => it.metadata.number === entry.request.request.number)
      emit(
        'update:matchedBook',
        matchedBook
          ? {
              bookId: matchedBook.id,
              title: matchedBook.metadata.title,
              number: matchedBook.metadata.number,
            }
          : undefined,
      )
    }
  },
)
</script>
