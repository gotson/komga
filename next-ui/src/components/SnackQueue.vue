<template>
  <v-snackbar-queue
    v-model="messages"
    timer
    close-on-content-click
  />
</template>

<script setup lang="ts">
import { isMessageDescriptor, useMessagesStore } from '@/stores/messages'
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
      return {
        text: text,
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
