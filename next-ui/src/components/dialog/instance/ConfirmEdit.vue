<template>
  <DialogConfirmEdit
    v-bind="confirmEdit.dialogProps"
    :activator="confirmEdit.activator"
    v-model:record="confirmEdit.record"
    @update:record="confirmEdit.recordUpdatedCallback()"
  >
    <template #text="{ proxyModel }">
      <component
        :is="confirmEdit.slot.component"
        v-bind="confirmEdit.slot.props"
        v-model="proxyModel.value"
      />
    </template>
  </DialogConfirmEdit>
</template>

<script setup lang="ts">
/**
 * Single instance of DialogConfirmEdit, mounted under App.
 * Communication from other components is done via useDialogsStore.confirmEdit
 */
import { useDialogsStore } from '@/stores/dialogs'
import { storeToRefs } from 'pinia'

const { confirmEdit } = storeToRefs(useDialogsStore())
</script>

<style scoped></style>
