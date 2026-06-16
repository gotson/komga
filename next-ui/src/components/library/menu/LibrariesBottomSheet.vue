<template>
  <ItemMenuBottomSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
  />
</template>

<script setup lang="ts">
import { createOrderCompareFn } from '@/functions/sort'
import { librariesActionGroups } from '@/types/action'
import { useLibrariesActions } from '@/composables/library/useLibrariesActions'

const isShown = defineModel<boolean>({ default: false })

function afterClick() {
  isShown.value = false
}
const { actions } = useLibrariesActions(afterClick)
const actionsDefault = computed(() =>
  actions.value
    .filter((it) => librariesActionGroups.default.includes(it.action))
    .toSorted(createOrderCompareFn(librariesActionGroups.default, (it) => it.action.toString())),
)
const actionsManagement = computed(() =>
  actions.value
    .filter((it) => librariesActionGroups.management.includes(it.action))
    .toSorted(createOrderCompareFn(librariesActionGroups.management, (it) => it.action.toString())),
)
</script>

<script lang="ts"></script>

<style scoped></style>
