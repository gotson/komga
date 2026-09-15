<template>
  <v-data-table
    :items="book.pages"
    :headers="pagesHeaders"
    disable-sort
    fixed-header
    mobile-breakpoint="md"
    @click:row="(_event: unknown, row: any) => emit('goToPage', row.item.number)"
  >
    <template #[`item.number`]="{ value }">
      {{ value }}
    </template>

    <template #[`item.page`]="{ item }">
      <BookComparePageDetails
        :page="item"
        class="pa-2"
      />
    </template>
  </v-data-table>
</template>

<script setup lang="ts">
import type { BookDetails } from '@/types/BookDetails'
import { useIntl } from 'vue-intl'

defineProps<{
  book: BookDetails
}>()

const emit = defineEmits<{
  goToPage: [number: number]
}>()

const intl = useIntl()

const pagesHeaders = computed(
  () =>
    [
      {
        title: intl.formatMessage({
          description: 'Page list table: table header - page number',
          defaultMessage: 'Page number',
          id: 'tA65z3',
        }),
        key: 'number',
        align: 'center',
      },
      {
        title: intl.formatMessage({
          description: 'Page list table: table header - page details',
          defaultMessage: 'Details',
          id: 'O6qNva',
        }),
        key: 'page',
        align: 'center',
      },
    ] as const, // workaround for https://github.com/vuetifyjs/vuetify/issues/18901
)
</script>
