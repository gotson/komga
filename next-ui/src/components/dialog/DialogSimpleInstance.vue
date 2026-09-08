<template>
  <DialogSimple
    v-model="showDialog"
    v-bind="simple.dialogProps"
    :loading="loading"
    :activator="simple.activator"
    @cancel="simple.callback('cancel', hideDialog, setLoading)"
  >
    <template #text>
      <component
        :is="simple.slot.component"
        v-bind="simple.slot.props"
        class="mt-1"
        v-on="simple.slot.handlers || {}"
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

const showDialog = ref<boolean>(false)
const loading = ref<boolean>(false)

const { simple } = storeToRefs(useDialogsStore())

function hideDialog() {
  showDialog.value = false
  loading.value = false
}

function setLoading(isLoading: boolean) {
  loading.value = isLoading
}

watch(
  () => simple.value.dialogProps.shown,
  (newShow) => {
    if (newShow != undefined) {
      showDialog.value = newShow
      simple.value.dialogProps.shown = undefined
    }
  },
)
</script>

<style scoped></style>
