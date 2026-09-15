<template>
  <v-tabs
    v-model="currentTab"
    :items="tabs"
    show-arrows
  >
    <template #tab="{ item }">
      <v-tab
        v-bind="item"
        rounded="0"
      />
    </template>

    <template #[`item.1`]>
      <BookCompareInfo
        :book="book"
        class="pa-4"
      />
    </template>

    <template #[`item.2`]>
      <BookDetailsPages
        :book="book"
        @go-to-page="goToPage"
      />
    </template>

    <template #[`item.3`]>
      <BookDetailsVisual
        v-model:page-number="pageBook"
        :pages="book.pages"
      />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import type { BookDetails } from '@/types/BookDetails'

const intl = useIntl()

const { book } = defineProps<{
  book: BookDetails
}>()

const currentTab = ref(1)

const tabs = computed(() => [
  {
    text: intl.formatMessage({
      description: 'Book Details: General',
      defaultMessage: 'General',
      id: '9GaR82',
    }),
    value: 1,
  },
  {
    text: intl.formatMessage({
      description: 'Book Details: Pages',
      defaultMessage: 'Pages',
      id: 'pz7ZlL',
    }),
    value: 2,
    disabled: !hasPages.value,
  },
  {
    text: intl.formatMessage({
      description: 'Book Details: Visual',
      defaultMessage: 'Visual',
      id: 'rw5pw5',
    }),
    value: 3,
    disabled: !hasPages.value,
  },
])

const pageBook = ref(1)

function goToPage(number: number) {
  if (hasPages.value) {
    pageBook.value = number
    currentTab.value = 3
  }
}

const hasPages = computed(() => book.pages.length > 0)
</script>
