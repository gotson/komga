<template>
  <DialogSimple
    v-model="showDialog"
    v-bind="simple.dialogProps"
    :loading="loading"
    :activator="simple.activator"
  >
    <template #text>
      <component
        :is="simple.slot.component"
        v-bind="simple.slot.props"
        class="mt-1"
        v-on="simple.slot.handlers"
      />
    </template>
  </DialogSimple>
</template>

<script setup lang="ts">
/**
 * Single instance of DialogSimple, mounted under App.
 * Communication from other components is done via useDialogsStore.simple
 */
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'
import { syncRefs } from '@vueuse/core'

const showDialog = ref<boolean>(false)
const loading = ref<boolean>(false)

const { simple } = storeToRefs(useDialogsStore())

syncRefs(
  toRef(() => simple.value.dialogProps.shown),
  showDialog,
)
</script>

<style scoped></style>
