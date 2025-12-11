<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :max-width="maxWidth"
    :fullscreen="fullscreen"
    :transition="fullscreen ? 'dialog-bottom-transition' : undefined"
    :scrollable="scrollable"
    :aria-label="title"
  >
    <template #default="{ isActive }">
      <v-card
        :title="title"
        :subtitle="subtitle"
        :loading="loading"
      >
        <template #text>
          <slot name="text" />
        </template>

        <template #actions>
          <v-spacer />
          <v-btn
            :text="
              $formatMessage({
                description: 'Simple dialog: Close button',
                defaultMessage: 'Close',
                id: 'Wivz5J',
              })
            "
            @click="isActive.value = false"
          />
        </template>
      </v-card>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import type { DialogSimpleProps } from '@/types/dialog'

const showDialog = defineModel<boolean>('dialog', { required: false, default: false })

const {
  title = undefined,
  subtitle = undefined,
  maxWidth = undefined,
  activator = undefined,
  loading = false,
  fullscreen = undefined,
  scrollable = undefined,
} = defineProps<DialogSimpleProps>()
</script>
