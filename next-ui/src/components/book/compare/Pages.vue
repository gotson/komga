<template>
  <v-data-table
    :items="items"
    :headers="pagesHeaders"
    disable-sort
    fixed-header
    mobile-breakpoint="md"
    v-on="tableEvents"
  >
    <template #[`item.left`]="{ value }">
      <BookComparePageDetails
        v-if="value"
        :page="value"
        class="pa-2"
      />
      <v-icon
        v-else
        icon="i-mdi:image-remove-outline"
        size="x-large"
        color="medium-emphasis"
      />
    </template>

    <template #[`item.right`]="{ value }">
      <BookComparePageDetails
        v-if="value"
        :page="value"
        class="pa-2"
      />
      <v-icon
        v-else
        icon="i-mdi:image-remove-outline"
        size="x-large"
        color="medium-emphasis"
      />
    </template>

    <template #[`item.number`]="{ value }">
      {{ value }}
    </template>
  </v-data-table>
</template>

<script setup lang="ts">
import type { BookDetails, PageDtoWithUrl } from '@/types/BookDetails'
import { useIntl } from 'vue-intl'

const { left, right, clickableRows, labelLeft, labelRight } = defineProps<{
  left: BookDetails
  right: BookDetails
  clickableRows?: boolean
  labelLeft?: string
  labelRight?: string
}>()

const emit = defineEmits<{
  goToPage: [number: number]
}>()

const intl = useIntl()

const maxPageCount = computed(() => Math.max(left.pages.length, right.pages.length))

type Item = {
  number: number
  left?: PageDtoWithUrl
  right?: PageDtoWithUrl
}

const items = computed(() =>
  Array.from(
    { length: maxPageCount.value },
    (v, k) =>
      ({
        number: k + 1,
        left: left.pages.at(k),
        right: right.pages.at(k),
      }) satisfies Item,
  ),
)

const pagesHeaders = computed(
  () =>
    [
      {
        title:
          labelLeft ??
          intl.formatMessage({
            description: 'Page comparison table: table header - left',
            defaultMessage: 'Left',
            id: 'AoSCGy',
          }),
        key: 'left',
        align: 'center',
        width: '45%',
      },
      {
        title: intl.formatMessage({
          description: 'Page comparison table: table header - page number',
          defaultMessage: 'Page number',
          id: 'Icqm5y',
        }),
        key: 'number',
        align: 'center',
        width: '10%',
      },
      {
        title:
          labelRight ??
          intl.formatMessage({
            description: 'Page comparison table: table header - right',
            defaultMessage: 'Right',
            id: 'f3/J8+',
          }),
        key: 'right',
        align: 'center',
        width: '45%',
      },
    ] as const, // workaround for https://github.com/vuetifyjs/vuetify/issues/18901
)
// dynamic binding of events via v-on so that the cursor:pointer doesn't show if rows are not clickable
const tableEvents = computed(() => {
  if (!clickableRows) return {}

  return {
    'click:row': (_event: MouseEvent, { item }: { item: Item }) => {
      if (item?.number != null) {
        emit('goToPage', item.number)
      }
    },
  }
})
</script>
