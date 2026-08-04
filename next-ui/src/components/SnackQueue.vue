<template>
  <v-snackbar-queue
    v-model="messages"
    timer
    close-on-content-click
  >
    <template #text="{ item }">
      <div style="white-space: pre-line">
        <template v-if="typeof item === 'string'">{{ item }}</template>
        <template v-else>{{ item.text }}</template>
      </div>
    </template>

    <template #actions="{ item, props }">
      <v-btn
        v-if="isMessageWithActionRouter(item)"
        :to="item.action.to"
        :text="
          isMessageDescriptor(item.action.label)
            ? $formatMessage(item.action.label)
            : item.action.label
        "
        variant="text"
        color="inherit"
        @click.stop="props.onClick()"
      />
    </template>
  </v-snackbar-queue>
</template>

<script setup lang="ts">
import { isMessageDescriptor, isMessageWithActionRouter, useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'

const messagesStore = useMessagesStore()
const intl = useIntl()

const messages = computed({
  get() {
    // map messages to snackbar format
    return messagesStore.messages.map((it) => {
      if (typeof it === 'string') return it
      if (isMessageDescriptor(it)) return intl.formatMessage(it)
      const text = typeof it.message === 'string' ? it.message : intl.formatMessage(it.message)
      const title = it.titleMessage
        ? typeof it.titleMessage === 'string'
          ? it.titleMessage
          : intl.formatMessage(it.titleMessage)
        : undefined
      return {
        text: text,
        ...(title ? { title: title } : {}),
        ...it,
      }
    })
  },
  set(newValue) {
    // snackbar-queue replaces the full array when removing messages, so they go back in the queue, already formatted by intl as needed
    messagesStore.messages = newValue
  },
})
</script>

<script lang="ts"></script>

<style scoped></style>
