// Utilities
import { defineStore } from 'pinia'
import type { DialogConfirmEditProps, DialogConfirmProps, DialogSimpleProps } from '@/types/dialog'

interface DialogsState {
  confirmEdit: DialogConfirmEditActivation
  confirm: DialogConfirmActivation
  simple: DialogSimpleActivation
}

/**
 * Reusable dialogs.
 * The single instances of the dialogs are created under App, and can be triggered by using this store.
 */
export const useDialogsStore = defineStore('dialogs', {
  state: (): DialogsState => ({
    confirmEdit: {
      dialogProps: {},
      slot: {
        component: undefined,
        props: {},
        handlers: {},
      },
      record: undefined,
      callback: () => {},
    },
    confirm: {
      dialogProps: {},
      slotWarning: {
        component: undefined,
        props: {},
        handlers: {},
      },
      callback: () => {},
    },
    simple: {
      dialogProps: {},
      slot: {
        component: undefined,
        props: {},
        handlers: {},
      },
      callback: () => {},
    },
  }),
})

type DialogActivation<T> = {
  activator?: Element | string
  dialogProps: T
  callback: (hideDialog: () => void, setLoading: (isLoading: boolean) => void) => void
}

type DialogConfirmEditActivation = DialogActivation<DialogConfirmEditProps> & {
  slot: ComponentWithProps
  record?: unknown
}

type DialogConfirmActivation = DialogActivation<DialogConfirmProps> & {
  slotWarning: ComponentWithProps
}

type DialogSimpleActivation = DialogActivation<DialogSimpleProps> & {
  slot: ComponentWithProps
}

type ComponentWithProps = {
  component?: Component
  props?: object
  handlers?: object
}
