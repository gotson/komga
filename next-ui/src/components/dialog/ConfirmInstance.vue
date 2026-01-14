<template>
  <DialogConfirm
    v-model="showDialog"
    :loading="loading"
    v-bind="confirm.dialogProps"
    :activator="confirm.activator"
    @confirm="confirm.callback(hideDialog, setLoading)"
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
import { syncRefs } from '@vueuse/core'

const showDialog = ref<boolean>(false)
const loading = ref<boolean>(false)

const { confirm } = storeToRefs(useDialogsStore())

function hideDialog() {
  showDialog.value = false
  loading.value = false
}

function setLoading(isLoading: boolean) {
  loading.value = isLoading
}

syncRefs(
  toRef(() => confirm.value.dialogProps.shown),
  showDialog,
)
</script>

<style scoped></style>
