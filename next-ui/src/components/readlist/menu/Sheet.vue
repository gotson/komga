<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useReadListActions } from '@/composables/readlist/useReadListActions'
import { type ReadListAction, readListActionGroups } from '@/types/readlist'
import { createOrderCompareFn } from '@/functions/sort'

const isShown = defineModel<boolean>({ default: false })

const { readList, excludeActions = [] } = defineProps<{
  activator: string | Element
  readList: components['schemas']['ReadListDto']
  excludeActions?: ReadListAction[]
}>()

function afterClick() {
  isShown.value = false
}
const { actions } = useReadListActions(() => readList, afterClick)
const actionsDefault = computed(() =>
  actions.value
    .filter(
      (it) =>
        readListActionGroups.default.includes(it.action) && !excludeActions.includes(it.action),
    )
    .toSorted(createOrderCompareFn(readListActionGroups.default, (it) => it.action.toString())),
)
const actionsManagement = computed(() =>
  actions.value
    .filter(
      (it) =>
        readListActionGroups.management.includes(it.action) && !excludeActions.includes(it.action),
    )
    .toSorted(createOrderCompareFn(readListActionGroups.management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
