<template>
  <ItemMenuSheet
    v-model="isShown"
    :actions="actionsDefault"
    :manage-actions="actionsManagement"
    :activator="activator"
  />
</template>

<script setup lang="ts">
import { createOrderCompareFn } from '@/functions/sort'
import { useLibrariesActions } from '@/composables/library/useLibrariesActions'
import { librariesActionGroups } from '@/types/action/libraries'
const isShown = defineModel<boolean>({ default: false })

defineProps<{
  activator: string | Element
}>()
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
