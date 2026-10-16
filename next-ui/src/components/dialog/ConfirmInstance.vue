<template>
  <DialogConfirm
    v-model="showDialog"
    :loading="loading"
    v-bind="confirm.dialogProps"
    :activator="confirm.activator"
    @cancel="confirm.callback('cancel', hideDialog, setLoading)"
    @confirm="confirm.callback('confirm', hideDialog, setLoading)"
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

watch(
  () => confirm.value.dialogProps.shown,
  (newShow) => {
    if (newShow != undefined) {
      showDialog.value = newShow
      confirm.value.dialogProps.shown = undefined
    }
  },
)
</script>
