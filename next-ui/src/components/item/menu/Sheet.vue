<template>
  <ItemMenu
    v-if="display.smAndUp.value"
    v-click-outside="isShown = false"
    :actions="actions"
    :manage-actions="manageActions"
    :activator="activator"
  />
  <ItemMenuBottomSheet
    v-if="display.xs.value"
    v-model="isShown"
    :actions="actions"
    :manage-actions="manageActions"
  />
</template>

<script setup lang="ts">
import { useDisplay } from 'vuetify/framework'
import type { Action } from '@/types/action/action'

const isShown = defineModel<boolean>({ default: false })

const { actions = [], manageActions = [] } = defineProps<{
  activator: string | Element
  actions?: Action<unknown>[]
  manageActions?: Action<unknown>[]
}>()

const display = useDisplay()
</script>

<script lang="ts"></script>

<style scoped></style>
