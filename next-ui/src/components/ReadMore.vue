<template>
  <LineClamp
    style="white-space: pre-wrap"
    class="text-body-large"
    :text="text"
    :max-lines="lines || defaultMaxLines"
    boundary="word"
  >
    <template #after="{ clamped, toggle, expanded }">
      <v-btn
        v-if="clamped || expanded"
        variant="text"
        class="ms-1"
        density="compact"
        :append-icon="expanded ? 'i-mdi:chevron-up' : 'i-mdi:chevron-down'"
        :text="$formatMessage(expanded ? commonMessages.showLess : commonMessages.showMore)"
        @click="toggle"
      />
    </template>
  </LineClamp>
</template>

<script setup lang="ts">
import { commonMessages } from '@/utils/i18n/common-messages'
import { LineClamp } from 'vue-clamp'

const defaultMaxLines = 4

defineProps<{
  text?: string
  lines?: number
}>()
</script>

<script lang="ts"></script>

<style scoped></style>
