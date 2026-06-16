<template>
  <ItemMenuBottomSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { createOrderCompareFn } from '@/functions/sort'
import { useLibraryActions } from '@/composables/library/useLibraryActions'
import { libraryActionGroups } from '@/types/action'

const isShown = defineModel<boolean>({ default: false })

const { library } = defineProps<{
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
