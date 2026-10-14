<template>
  <div class="d-flex ga-2">
    <v-btn-group
      border
      color=""
      size="small"
    >
      <v-btn
        v-ktooltip:bottom="
          $formatMessage({
            description: 'Organize list: Undo button tooltip',
            defaultMessage: 'Undo',
            id: 'M4STMh',
          })
        "
        :icon="isRtl ? 'i-mdi:redo-variant' : 'i-mdi:undo-variant'"
        :disabled="!canUndo"
        @click="emit('undo')"
      />
      <v-btn
        v-ktooltip:bottom="
          $formatMessage({
            description: 'Organize list: Redo button tooltip',
            defaultMessage: 'Redo',
            id: 'veqjHc',
          })
        "
        :icon="isRtl ? 'i-mdi:undo-variant' : 'i-mdi:redo-variant'"
        :disabled="!canRedo"
        @click="emit('redo')"
      />
    </v-btn-group>
  </div>
</template>

<script setup lang="ts">
import { useRtl } from 'vuetify/framework'

const { isRtl } = useRtl()

defineProps<{
  canUndo?: boolean
  canRedo?: boolean
}>()

const emit = defineEmits<{
  undo: []
  redo: []
}>()
</script>
