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
    :exclude-actions="exclude"
  />
</template>

<script setup lang="ts">
import { useBookActions } from '@/composables/book/useBookActions'
import { createOrderCompareFn } from '@/functions/sort'
import { commonMessages } from '@/utils/i18n/common-messages'
import { ActionName } from '@/types/action/action'
import type { BookDto } from '@/generated/openapi'

const props = defineProps<{
  book: BookDto
}>()

const id = useId()
const { actions } = useBookActions(() => props.book)

const bottomSheet = ref(false)

const prominent = [
  ActionName.OPEN_READER_INCOGNITO,
  ActionName.MARK_READ,
  ActionName.MARK_UNREAD,
  ActionName.EDIT_BOOK,
]
const exclude = [...prominent, ActionName.OPEN_READER]

const readAction = computed(() => actions.value.find((it) => it.action === ActionName.OPEN_READER))
const prominentActions = computed(() =>
  actions.value
    .filter((it) => prominent.includes(it.action))
    .toSorted(createOrderCompareFn(prominent, (it) => it.action.toString())),
)
const hasExtra = computed(() => actions.value.some((it) => !exclude.includes(it.action)))
</script>
