// Utilities
import { defineStore } from 'pinia'

type Message =
  | {
      text: string
      color?: string
      timer?: boolean | 'top' | 'bottom'
      timeout?: string | number
    }
  | string

export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: [] as Message[],
  }),
})
