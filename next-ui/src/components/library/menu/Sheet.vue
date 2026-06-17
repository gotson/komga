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
import { useLibraryActions } from '@/composables/library/useLibraryActions'
import { createOrderCompareFn } from '@/functions/sort'

import { libraryActionGroups } from '@/types/action/library'
const isShown = defineModel<boolean>({ default: false })

const { library } = defineProps<{
  activator: string | Element
  library: components['schemas']['LibraryDto']
}>()
function afterClick() {
  isShown.value = false
}
const { actions } = useLibraryActions(() => library, afterClick)
const actionsDefault = computed(() =>
  actions.value
    .filter((it) => libraryActionGroups.default.includes(it.action))
    .toSorted(createOrderCompareFn(libraryActionGroups.default, (it) => it.action.toString())),
)
const actionsManagement = computed(() =>
  actions.value
    .filter((it) => libraryActionGroups.management.includes(it.action))
    .toSorted(createOrderCompareFn(libraryActionGroups.management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
