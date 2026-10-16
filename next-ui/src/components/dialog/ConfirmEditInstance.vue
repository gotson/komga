<template>
  <DialogConfirmEdit
    v-model="showDialog"
    v-bind="confirmEdit.dialogProps"
    v-model:record="confirmEdit.record"
    :loading="loading"
    :activator="confirmEdit.activator"
    @cancel="confirmEdit.callback('cancel', hideDialog, setLoading)"
    @update:record="confirmEdit.callback('confirm', hideDialog, setLoading)"
    @submit-failed="submitFailed = true"
  >
    <template #text="{ proxyModel }">
      <component
        :is="confirmEdit.slots.default.component"
        v-bind="confirmEdit.slots.default.props"
        v-model="proxyModel.value"
        v-model:submit-failed="submitFailed"
        class="mt-1"
        v-on="confirmEdit.slots.default.handlers || {}"
      />
    </template>

    <template
      v-if="confirmEdit.slots.prependActions"
      #prepend-actions
    >
      <component
        :is="confirmEdit.slots.prependActions.component"
        v-bind="confirmEdit.slots.prependActions.props"
        v-on="confirmEdit.slots.prependActions.handlers || {}"
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
const submitFailed = ref<boolean>(false)

const { confirmEdit } = storeToRefs(useDialogsStore())

function hideDialog() {
  showDialog.value = false
  loading.value = false
  submitFailed.value = false
}

function setLoading(isLoading: boolean) {
  loading.value = isLoading
}

watch(
  () => confirmEdit.value.dialogProps.shown,
  (newShow) => {
    if (newShow != undefined) {
      showDialog.value = newShow
      confirmEdit.value.dialogProps.shown = undefined
    }
  },
)
</script>
