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
      <BookCompareGeneral
        :left="left"
        :right="right"
        :is-upgrade="isUpgrade"
        class="pa-4"
      />
    </template>

    <template #[`item.2`]>
      <BookComparePages
        :left="left"
        :right="right"
        :clickable-rows="bothPages"
        :label-left="isUpgrade ? labelExisting : undefined"
        :label-right="isUpgrade ? labelCandidate : undefined"
        @go-to-page="goToPage"
      />
    </template>

    <template #[`item.3`]>
      <BookCompareVisual
        v-model:page-number-left="pageLeft"
        v-model:page-number-right="pageRight"
        :left="left.pages"
        :right="right.pages"
      />
    </template>
  </v-tabs>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import type { BookDetails } from '@/types/BookDetails'

const intl = useIntl()

const {
  left,
  right,
  isUpgrade = false,
} = defineProps<{
  left: BookDetails
  right: BookDetails
  isUpgrade?: boolean
}>()

const currentTab = ref(1)

const tabs = computed(() => [
  {
    text: intl.formatMessage({
      description: 'Book Compare: General',
      defaultMessage: 'General',
      id: 'iVYrha',
    }),
    value: 1,
  },
  {
    text: intl.formatMessage({
      description: 'Book Compare: Pages',
      defaultMessage: 'Pages',
      id: 'FH0CJ+',
    }),
    value: 2,
    disabled: noPages.value,
  },
  {
    text: intl.formatMessage({
      description: 'Book Compare: Visual',
      defaultMessage: 'Visual',
      id: 'Er4D87',
    }),
    value: 3,
    disabled: oneSided.value,
  },
])

const pageLeft = ref(1)
const pageRight = ref(1)

function goToPage(number: number) {
  if (bothPages.value) {
    pageLeft.value = number
    pageRight.value = number
    currentTab.value = 3
  }
}

const bothPages = computed(() => left.pages.length > 0 && right.pages.length > 0)
const noPages = computed(() => left.pages.length === 0 && right.pages.length === 0)
const oneSided = computed(() => left.pages.length === 0 || right.pages.length === 0)

const labelExisting = intl.formatMessage({
  description: 'Book Compare: existing book label',
  defaultMessage: 'Existing',
  id: 'uBqZMx',
})
const labelCandidate = intl.formatMessage({
  description: 'Book Compare: upgrade book label',
  defaultMessage: 'Candidate',
  id: 'upo+2b',
})
</script>
