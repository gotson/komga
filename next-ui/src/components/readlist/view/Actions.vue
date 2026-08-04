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
  <ReadlistMenuSheet
    v-model="bottomSheet"
    :read-list="readList"
    :activator="`#${id}`"
    :exclude-actions="exclude"
  />
</template>

<script setup lang="ts">
import { commonMessages } from '@/utils/i18n/common-messages'
import { useReadListActions } from '@/composables/readlist/useReadListActions'
import { ActionName } from '@/types/action/action'
import type { ReadListDto } from '@/generated/openapi'
import { createOrderCompareFn } from '@/functions/sort'

const props = defineProps<{
  readList: ReadListDto
}>()

const id = useId()
const { actions } = useReadListActions(() => props.readList)

const bottomSheet = ref(false)
const prominent = computed(
  () => [ActionName.OpenReaderIncognito, ActionName.EditReadList] as ActionName[],
)

const exclude = [...prominent.value, ActionName.OpenReader]

const prominentActions = computed(() =>
  actions.value
    .filter((it) => prominent.value.includes(it.action))
    .toSorted(createOrderCompareFn(prominent.value, (it) => it.action.toString())),
)
const readAction = computed(() => actions.value.find((it) => it.action === ActionName.OpenReader))
const hasExtra = computed(() => actions.value.some((it) => !exclude.includes(it.action)))
</script>
