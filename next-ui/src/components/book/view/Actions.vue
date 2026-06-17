<template>
  <div class="d-flex ga-2">
    <v-btn
      v-if="readAction"
      :prepend-icon="readAction.icon"
      :text="readAction.title"
      :disabled="readAction.disabled"
      @click="readAction.onClick?.()"
    />

    <v-icon-btn
      v-for="action in prominentActions"
      :key="action.action"
      v-tooltip:bottom="action.title"
      v-bind="action"
    />

    <v-icon-btn
      v-if="hasExtra"
      :id="id"
      v-tooltip:bottom="$formatMessage(commonMessages.buttonMore)"
      icon="i-mdi:dots-horizontal"
      @click="bottomSheet = true"
    />
  </div>
  <BookMenuSheet
    v-model="bottomSheet"
    :book="book"
    :activator="`#${id}`"
    :exclude-actions="excludeActions"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useBookActions } from '@/composables/book/useBookActions'
import { BookAction, bookActionGroups } from '@/types/action/book'
import { createOrderCompareFn } from '@/functions/sort'
import { commonMessages } from '@/utils/i18n/common-messages'

const props = defineProps<{
  book: components['schemas']['BookDto']
}>()

const id = useId()
const { actions } = useBookActions(() => props.book)

const bottomSheet = ref(false)

const prominentActions = computed(() =>
  actions.value
    .filter((it) => bookActionGroups.bookView.includes(it.action))
    .toSorted(createOrderCompareFn(bookActionGroups.bookView, (it) => it.action.toString())),
)
const readAction = computed(() => actions.value.find((it) => it.action === BookAction.OPEN_READER))
const excludeActions = [...bookActionGroups.bookView, BookAction.OPEN_READER]
const hasExtra = computed(() => actions.value.some((it) => !excludeActions.includes(it.action)))
</script>
