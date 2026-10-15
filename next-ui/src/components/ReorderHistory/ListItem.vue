<template>
  <v-list-item
    :class="{
      [`${handleClass} cursor-grab`]: !isTouchPrimary && !disableReorder,
    }"
    :title="title"
    :subtitle="subtitle"
  >
    <template #prepend>
      <div class="d-flex ga-4 align-center">
        <v-icon
          icon="i-mdi:minus-circle-outline"
          size="small"
          @click="emit('delete')"
        />
        <v-avatar :image="avatar" />
      </div>
    </template>

    <template
      v-if="!disableReorder"
      #append
    >
      <v-icon
        icon="i-mdi:drag"
        :class="{
          [`${handleClass} cursor-grab`]: isTouchPrimary && !disableReorder,
        }"
      />
    </template>
  </v-list-item>
</template>

<script setup lang="ts">
import { usePrimaryInput } from '@/composables/device'

const { isTouchPrimary } = usePrimaryInput()

defineProps<{
  handleClass: string
  disableReorder: boolean
  title?: string
  subtitle?: string
  avatar?: string
}>()

const emit = defineEmits<{
  delete: []
}>()
</script>
