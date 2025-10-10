// Utilities
import { defineStore } from 'pinia'

type Message =
  | {
      text: string
      color?: string
      timer?: string | boolean
      timeout?: string | number
    }
  | string

export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: [] as Message[],
  }),
})
