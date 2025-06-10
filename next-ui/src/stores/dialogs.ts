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
      callback: () => {},
    } as DialogConfirmEditActivation,
    confirm: {
      dialogProps: {},
      slotWarning: {
        component: undefined,
        props: {},
      },
      callback: () => {},
    } as DialogConfirmActivation,
  }),
})

interface DialogActivation<T> {
  activator?: Element | string
  dialogProps: T
  callback: (hideDialog: () => void, setLoading: (isLoading: boolean) => void) => void
}

interface DialogConfirmEditActivation extends DialogActivation<DialogConfirmEditProps> {
  slot: ComponentWithProps
  record?: unknown
}

interface DialogConfirmActivation extends DialogActivation<DialogConfirmProps> {
  slotWarning: ComponentWithProps
}

interface ComponentWithProps {
  component?: Component
  props: object
}
