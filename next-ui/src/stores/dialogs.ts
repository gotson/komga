// Utilities
import { defineStore } from 'pinia'
import type { DialogConfirmEditProps } from '@/components/dialog/ConfirmEdit.vue'
import type { DialogConfirmProps } from '@/components/dialog/Confirm.vue'

/**
 * Reusable dialogs.
 * The single instances of the dialogs are created under App, and can be triggered by using this store.
 */
export const useDialogsStore = defineStore('dialogs', {
  state: () => ({
    confirmEdit: {
      dialogProps: {},
      slot: {
        component: undefined,
        props: {},
      },
      record: undefined,
      recordUpdatedCallback: () => {},
    } as DialogConfirmEditActivation,
    confirm: {
      dialogProps: {},
      slotWarning: {
        component: undefined,
        props: {},
      },
      confirmCallback: () => {},
    } as DialogConfirmActivation,
  }),
})

interface DialogConfirmEditActivation {
  activator?: Element | string
  dialogProps: DialogConfirmEditProps
  slot: ComponentWithProps
  record?: unknown
  recordUpdatedCallback: () => void
}

interface DialogConfirmActivation {
  activator?: Element | string
  dialogProps: DialogConfirmProps
  slotWarning: ComponentWithProps
  confirmCallback: () => void
}

interface ComponentWithProps {
  component?: Component
  props: object
}
