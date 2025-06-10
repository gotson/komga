<template>
  <DialogConfirmEdit
    v-model="showDialog"
    :loading="loading"
    v-bind="confirmEdit.dialogProps"
    :activator="confirmEdit.activator"
    v-model:record="confirmEdit.record"
    @update:record="confirmEdit.callback(hideDialog, setLoading)"
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

const showDialog = ref<boolean>(false)
const loading = ref<boolean>(false)

const { confirmEdit } = storeToRefs(useDialogsStore())

function hideDialog() {
  showDialog.value = false
  loading.value = false
}

function setLoading(isLoading: boolean) {
  loading.value = isLoading
}
</script>

<style scoped></style>
