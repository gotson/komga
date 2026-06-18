// Utilities
import { defineStore } from 'pinia'
import type { MessageDescriptor } from 'vue-intl'
import * as v from 'valibot'

const MessageDescriptorDiscriminator = v.looseObject({
  defaultMessage: v.string(),
})

export function isMessageDescriptor(item: unknown): item is MessageDescriptor {
  return v.is(MessageDescriptorDiscriminator, item)
}

type Message =
  | {
      message: MessageDescriptor | string
      color?: string
      timer?: boolean | 'top' | 'bottom'
      timeout?: string | number
    }
  | MessageDescriptor
  | string

export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: [] as Message[],
  }),
})
