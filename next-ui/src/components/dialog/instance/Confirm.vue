<template>
  <DialogConfirm
    v-bind="confirm.dialogProps"
    :activator="confirm.activator"
    @confirm="confirm.confirmCallback()"
  >
    <template #warning>
      <component
        :is="confirm.slotWarning.component"
        v-bind="confirm.slotWarning.props"
      />
    </template>
  </DialogConfirm>
</template>

<script setup lang="ts">
/**
 * Single instance of DialogConfirm, mounted under App.
 * Communication from other components is done via useDialogsStore.confirm
 */
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'

const { confirm } = storeToRefs(useDialogsStore())
</script>

<style scoped></style>
