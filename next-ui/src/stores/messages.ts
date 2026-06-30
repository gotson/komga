// Utilities
import { defineStore } from 'pinia'
import type { MessageDescriptor } from 'vue-intl'
import * as v from 'valibot'
import type { RouteLocationNormalized, RouteLocationRaw } from 'vue-router'

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
      action?: MessageActionRouter
    }
  | MessageDescriptor
  | string

type MessageActionRouter = {
  to: RouteLocationRaw
  label: string | MessageDescriptor
}

const MessageActionRouterDiscriminator = v.looseObject({
  action: v.looseObject({
    to: v.union([v.looseObject({}), v.string()]),
  }),
})

export function isMessageWithActionRouter(
  item: unknown,
): item is Message & { action: MessageActionRouter } {
  return v.is(MessageActionRouterDiscriminator, item)
}

export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: [] as Message[],
  }),
})
