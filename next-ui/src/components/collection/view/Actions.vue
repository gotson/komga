<template>
  <div class="d-flex ga-2">
    <v-btn
      v-if="editAction"
      :prepend-icon="editAction.icon"
      :text="editAction.title"
      :disabled="editAction.disabled"
      @click="editAction.onClick?.()"
      @mouseenter="editAction?.onMouseenter?.($event)"
    />

    <v-icon-btn
      v-for="action in prominentActions"
      :key="action.action"
      v-ktooltip:bottom="action.title"
      v-bind="action"
    />

    <v-icon-btn
      v-if="hasExtra"
      :id="id"
      v-ktooltip:bottom="$formatMessage(commonMessages.buttonMore)"
      icon="i-mdi:dots-horizontal"
      @click="bottomSheet = true"
    />
  </div>
  <CollectionMenuSheet
    v-model="bottomSheet"
    :collection="collection"
    :activator="`#${id}`"
    :exclude-actions="exclude"
  />
</template>

<script setup lang="ts">
import { commonMessages } from '@/utils/i18n/common-messages'
import { useCollectionActions } from '@/composables/collection/useCollectionActions'
import { ActionName } from '@/types/action/action'
import type { CollectionDto } from '@/generated/openapi'
import { createOrderCompareFn } from '@/functions/sort'

const props = defineProps<{
  collection: CollectionDto
}>()

const id = useId()
const { actions } = useCollectionActions(() => props.collection)

const bottomSheet = ref(false)
const prominent = [ActionName.EditCollectionElements] as ActionName[]

const prominentActions = computed(() =>
  actions.value
    .filter((it) => prominent.includes(it.action))
    .toSorted(createOrderCompareFn(prominent, (it) => it.action.toString())),
)

const editAction = computed(() =>
  actions.value.find((it) => it.action === ActionName.EditCollection),
)

const exclude = [...prominent, ActionName.EditCollection] as ActionName[]
const hasExtra = computed(() => actions.value.some((it) => !exclude.includes(it.action)))
</script>
