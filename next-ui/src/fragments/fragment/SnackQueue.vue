<template>
  <v-snackbar-queue
    v-model="messagesStore.messages"
    timer
  />
</template>

<script setup lang="ts">
import { useMessagesStore } from '@/stores/messages'

const messagesStore = useMessagesStore()
</script>

<script lang="ts"></script>

<style scoped></style>
