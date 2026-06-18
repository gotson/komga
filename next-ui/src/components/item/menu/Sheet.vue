<template>
  <ItemMenu
    v-if="!isTouchPrimary"
    v-click-outside="isShown = false"
    :actions="actions"
    :manage-actions="manageActions"
    :activator="activator"
  />
  <ItemMenuBottomSheet
    v-if="isTouchPrimary"
    v-model="isShown"
    :title="sheetTitle"
    :actions="actions"
    :manage-actions="manageActions"
  />
</template>

<script setup lang="ts">
import { usePrimaryInput } from '@/composables/device'
import type { Action } from '@/types/action/action'

const isShown = defineModel<boolean>({ default: false })
const { isTouchPrimary } = usePrimaryInput()

const { actions = [], manageActions = [] } = defineProps<{
  activator: string | Element
  actions?: Action<unknown>[]
  manageActions?: Action<unknown>[]
  sheetTitle?: string
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
